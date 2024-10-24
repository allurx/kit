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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A Poller implementation that limits polling attempts based on a specified time duration and interval.
 * Polling stops either when the time duration expires or the termination condition is satisfied.
 * Supports custom sleeping behavior between polling attempts.
 * <p>Example usage of {@code IntervalBasedPoller}.</p>
 *
 * <pre>
 * {@code
 * IntervalBasedPoller poller = IntervalBasedPoller.builder()
 *         .timing(Duration.ofSeconds(3), Duration.ofMillis(300))
 *         .build();
 *
 * var ai = new AtomicInteger(0);
 * var num = poller.poll(() -> ai,AtomicInteger::incrementAndGet,i -> i == 6).get();
 *
 * // The final result should be 6 if the polling was successful.
 * System.out.println("Final result: " + num);
 * }
 * </pre>
 *
 * @author allurx
 */
public class IntervalBasedPoller extends BasePoller {

    /**
     * The clock used to track the polling duration.
     */
    private final Clock clock;

    /**
     * The total duration allowed for polling.
     */
    private final Duration duration;

    /**
     * The interval between each polling attempt.
     */
    private final Duration interval;

    /**
     * The sleeper used to pause between polling attempts.
     */
    private final Sleeper sleeper;

    private IntervalBasedPoller(IntervalBasedPollerBuilder builder) {
        super(builder.ignoredExceptions, builder.logger);
        this.clock = builder.clock;
        this.duration = builder.duration;
        this.interval = builder.interval;
        this.sleeper = builder.sleeper;
    }

    @Override
    public <A, B> PollResult<B> poll(Supplier<? extends A> supplier,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        check(function, predicate);
        int cnt = 0;
        B result;
        Instant endInstant = clock.instant().plus(duration);
        while (true) {

            cnt++;

            // Break if predicate is satisfied after executing function
            if (predicate.test(result = execute(supplier.get(), function))) break;

            // Break if current time plus interval is after endInstant
            if (clock.instant().plus(interval).isAfter(endInstant)) break;

            // Sleep for the specified interval
            sleeper.sleep(interval);
        }
        return new PollResult<>(cnt, result);
    }

    /**
     * Creates a new builder instance for configuring and constructing a {@link IntervalBasedPoller}.
     *
     * @return a new {@link IntervalBasedPollerBuilder}
     */
    public static IntervalBasedPollerBuilder builder() {
        return new IntervalBasedPollerBuilder();
    }

    /**
     * IntervalBasedPollerBuilder is used to build a {@link IntervalBasedPoller}, which polls
     * at regular intervals for a specified duration.
     */
    public static class IntervalBasedPollerBuilder extends BasePollerBuilder<IntervalBasedPollerBuilder> {

        /**
         * Default constructor
         */
        public IntervalBasedPollerBuilder() {
        }

        private Clock clock = Clock.systemDefaultZone();
        private Duration duration = Duration.ZERO;
        private Duration interval = Duration.ZERO;
        private Sleeper sleeper = Sleeper.DEFAULT;

        /**
         * Configures the polling to use the system clock, with the specified duration and interval.
         *
         * @param duration the total time to continue polling
         * @param interval the time between polling attempts
         * @return the builder instance for chaining
         */
        public IntervalBasedPollerBuilder timing(Duration duration, Duration interval) {
            return timing(Clock.systemDefaultZone(), duration, interval);
        }

        /**
         * Configures the polling to use a custom clock, with the specified duration and interval.
         *
         * @param clock    the clock to use for timing
         * @param duration the total time to continue polling
         * @param interval the time between polling attempts
         * @return the builder instance for chaining
         */
        public IntervalBasedPollerBuilder timing(Clock clock, Duration duration, Duration interval) {
            this.clock = Objects.requireNonNull(clock, "The clock must not be null");
            this.duration = Objects.requireNonNull(duration, "The duration must not be null");
            this.interval = Objects.requireNonNull(interval, "The interval must not be null");
            return this;
        }

        /**
         * Sets the sleeper that will pause between polling attempts.
         *
         * @param sleeper the custom sleeper to use
         * @return the builder instance for chaining
         */
        public IntervalBasedPollerBuilder sleeper(Sleeper sleeper) {
            this.sleeper = Objects.requireNonNull(sleeper, "The sleeper must not be null");
            return this;
        }

        /**
         * Builds and returns a new {@link IntervalBasedPoller} instance.
         *
         * @return a new IntervalBasedPoller instance
         */
        public IntervalBasedPoller build() {
            return new IntervalBasedPoller(this);
        }
    }

}
