/*
 * Copyright 2024 allurx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.allurx.kit.base.concurrency;

import io.allurx.kit.base.Conditional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Repeats attempts without a delay until the predicate matches or the positive attempt limit is reached.
 * Ignored failures count toward the limit and pass null to the predicate.
 * This poller does not check the thread's interrupt flag; callbacks can implement their own cancellation.
 *
 * <pre>{@code
 * CountBasedPoller poller = CountBasedPoller.builder()
 *         .count(20)
 *         .build();
 *
 * var ai = new AtomicInteger(0);
 * var result = poller.poll(() -> ai, AtomicInteger::incrementAndGet, i -> i == 6);
 * // result.count() == 6 and result.get() == 6
 * }</pre>
 *
 * @author allurx
 */
public class CountBasedPoller extends BasePoller {

    private final int count;

    private CountBasedPoller(CountBasedPollerBuilder builder) {
        super(builder.ignoredExceptions, builder.logger);
        this.count = builder.count;
    }

    /**
     * {@inheritDoc}
     * The function and predicate are validated before the first attempt;
     * the supplier is validated when that attempt starts.
     */
    @Override
    public <A, B> PollResult<B> poll(Supplier<? extends A> supplier,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        check(function, predicate);
        int cnt = 0;
        B result = null;
        for (int i = 0; i < count; i++) {
            cnt++;
            if (predicate.test(result = execute(supplier, function))) break;
        }
        return new PollResult<>(cnt, result);
    }

    /**
     * Creates a new builder instance for configuring and constructing a {@link CountBasedPoller}.
     *
     * @return a new {@link CountBasedPollerBuilder}
     */
    public static CountBasedPollerBuilder builder() {
        return new CountBasedPollerBuilder();
    }

    /**
     * Configures an attempt-limited poller. A positive count must be set before building.
     *
     * @author allurx
     */
    public static class CountBasedPollerBuilder extends BasePollerBuilder<CountBasedPollerBuilder> {

        /**
         * Creates a builder with no polling count configured.
         */
        public CountBasedPollerBuilder() {
        }

        /**
         * The configured maximum count, or zero until {@link #count(int)} is called successfully.
         */
        private int count;

        /**
         * Sets the maximum number of polling attempts. Polling will stop either when the condition is met,
         * or when this count is reached.
         *
         * @param count the maximum number of polling attempts
         * @return the builder instance for chaining
         * @throws IllegalArgumentException if the count is less than or equal to 0
         */
        public CountBasedPollerBuilder count(int count) {
            Conditional.of(this)
                    .when(count > 0)
                    .consume(builder -> builder.count = count)
                    .orElse()
                    .throwIt(() -> new IllegalArgumentException("The maximum number of polling attempts must be greater than 0. Provided value: %s".formatted(count)));
            return this;
        }

        /**
         * Builds and returns a new {@link CountBasedPoller} instance.
         *
         * @return a new CountBasedPoller instance
         * @throws IllegalStateException if no positive count has been configured
         */
        public CountBasedPoller build() {
            if (count <= 0) {
                throw new IllegalStateException("A positive maximum polling count must be configured before building");
            }
            return new CountBasedPoller(this);
        }
    }

}
