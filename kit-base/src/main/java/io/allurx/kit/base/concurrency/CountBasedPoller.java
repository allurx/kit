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
 * A Poller implementation that limits the number of polling attempts.
 * Polling stops either when the specified maximum count is reached or the termination condition is satisfied.
 * <p>Example usage of CountBasedPoller.</p>
 * <pre>
 * {@code
 * CountBasedPoller poller = CountBasedPoller.builder()
 *         .count(20)
 *         .build();
 *
 * var ai = new AtomicInteger(0);
 * var num = poller.poll(() -> ai,AtomicInteger::incrementAndGet, i -> i == 6).get();
 *
 * // The final result should be 6 if the polling was successful.
 * System.out.println("Final result: " + num);
 * }
 * </pre>
 *
 * @author allurx
 */
public class CountBasedPoller extends BasePoller {

    /**
     * The maximum number of polling attempts.
     */
    private final int count;

    private CountBasedPoller(CountBasedPollerBuilder builder) {
        super(builder.ignoredExceptions, builder.logger);
        this.count = builder.count;
    }

    @Override
    public <A, B> PollResult<B> poll(Supplier<? extends A> supplier,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        check(function, predicate);
        int cnt = 0;
        B result = null;
        for (int i = 0; i < count; i++) {
            cnt++;
            if (predicate.test(result = execute(supplier.get(), function))) break;
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
     * CountBasedPollerBuilder is used to build a {@link CountBasedPoller} which performs polling
     * for a maximum number of iterations.
     */
    public static class CountBasedPollerBuilder extends BasePollerBuilder<CountBasedPollerBuilder> {

        /**
         * Default constructor
         */
        public CountBasedPollerBuilder() {
        }

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
            return Conditional.of(this)
                    .when(count > 0)
                    .consume(builder -> builder.count = count)
                    .orElse()
                    .throwIt(() -> new IllegalArgumentException("The maximum number of polling attempts must be greater than 0. Provided value: %s".formatted(count)))
                    .get();
        }

        /**
         * Builds and returns a new {@link CountBasedPoller} instance.
         *
         * @return a new CountBasedPoller instance
         */
        public CountBasedPoller build() {
            return new CountBasedPoller(this);
        }
    }

}
