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
 * Repeats attempts within a clock-based duration, sleeping a fixed interval after unsuccessful attempts.
 * The interval starts after the callbacks finish; it is not a fixed-rate schedule.
 *
 * <p>
 * Unless the current thread is already interrupted, the first attempt runs immediately, even with zero duration.
 * Polling stops when the termination condition is satisfied, the thread is interrupted, or no time remains.
 * Interruption preserves the interrupt flag and returns the last result and actual attempt count.
 * If interrupted before the first attempt, the result is null and the count is zero.
 * The deadline is checked again after sleeping; the poller does not interrupt callbacks already in progress.
 * Polling can finish before the deadline when a full sleep interval would extend beyond it.
 * Callback execution time counts toward the duration. Clock adjustments affect the deadline.
 *
 * <pre>{@code
 * IntervalBasedPoller poller = IntervalBasedPoller.builder()
 *         .timing(Duration.ofSeconds(3), Duration.ofMillis(300))
 *         .build();
 *
 * var ai = new AtomicInteger(0);
 * var result = poller.poll(() -> ai, AtomicInteger::incrementAndGet, i -> i == 6);
 * // Inspect result.get(): reaching the time limit does not imply success.
 * }</pre>
 *
 * @author allurx
 */
public class IntervalBasedPoller extends BasePoller {

    private final Clock clock;

    private final Duration duration;

    private final Duration interval;

    private final Sleeper sleeper;

    private IntervalBasedPoller(IntervalBasedPollerBuilder builder) {
        super(builder.ignoredExceptions, builder.logger);
        this.clock = builder.clock;
        this.duration = builder.duration;
        this.interval = builder.interval;
        this.sleeper = builder.sleeper;
    }

    /**
     * {@inheritDoc}
     * The function and predicate are validated even if the thread is already interrupted.
     * The supplier is validated only when an attempt starts.
     */
    @Override
    public <A, B> PollResult<B> poll(Supplier<? extends A> supplier,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        check(function, predicate);
        int cnt = 0;
        B result = null;
        Instant endInstant = clock.instant().plus(duration);
        do {
            if (Thread.currentThread().isInterrupted()) break;

            cnt++;

            // Check the termination condition after each attempt, including an ignored failure.
            if (predicate.test(result = execute(supplier, function))) break;

            // Do not sleep after cancellation or when the next interval exceeds the deadline.
            if (Thread.currentThread().isInterrupted() || clock.instant().plus(interval).isAfter(endInstant)) break;

            // Sleep may resume late, so recheck the deadline before another attempt.
            sleeper.sleep(interval);
        } while (clock.instant().isBefore(endInstant));
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
     * Configures a duration-limited poller. Defaults to the system clock, zero duration,
     * zero interval and {@link Sleeper#DEFAULT}, allowing one immediate attempt unless interrupted.
     *
     * @author allurx
     */
    public static class IntervalBasedPollerBuilder extends BasePollerBuilder<IntervalBasedPollerBuilder> {

        /**
         * Creates a builder with the default single-attempt timing configuration.
         */
        public IntervalBasedPollerBuilder() {
        }

        private Clock clock = Clock.systemDefaultZone();
        private Duration duration = Duration.ZERO;
        private Duration interval = Duration.ZERO;
        private Sleeper sleeper = Sleeper.DEFAULT;

        /**
         * Configures the polling to use the system clock, with the specified duration and interval.
         * Both durations may be zero. Invalid arguments leave the current timing configuration unchanged.
         *
         * @param duration the non-negative total time to continue polling
         * @param interval the non-negative time between polling attempts
         * @return the builder instance for chaining
         * @throws NullPointerException if duration or interval is null
         * @throws IllegalArgumentException if duration or interval is negative
         */
        public IntervalBasedPollerBuilder timing(Duration duration, Duration interval) {
            return timing(Clock.systemDefaultZone(), duration, interval);
        }

        /**
         * Configures the polling to use a custom clock, with the specified duration and interval.
         * Both durations may be zero. Invalid arguments leave the current timing configuration unchanged.
         * The clock must advance for a positive duration to expire; a fixed clock requires another
         * stopping condition or a test sleeper that advances a controllable clock.
         *
         * @param clock    the clock to use for timing
         * @param duration the non-negative total time to continue polling
         * @param interval the non-negative time between polling attempts
         * @return the builder instance for chaining
         * @throws NullPointerException if clock, duration, or interval is null
         * @throws IllegalArgumentException if duration or interval is negative
         */
        public IntervalBasedPollerBuilder timing(Clock clock, Duration duration, Duration interval) {
            Objects.requireNonNull(clock, "The clock must not be null");
            Objects.requireNonNull(duration, "The duration must not be null");
            Objects.requireNonNull(interval, "The interval must not be null");
            if (duration.isNegative()) {
                throw new IllegalArgumentException("The duration must not be negative");
            }
            if (interval.isNegative()) {
                throw new IllegalArgumentException("The interval must not be negative");
            }
            this.clock = clock;
            this.duration = duration;
            this.interval = interval;
            return this;
        }

        /**
         * Sets the sleeper invoked between unsuccessful attempts.
         * Its exceptions propagate; it should preserve the interrupt flag when interrupted.
         *
         * @param sleeper the custom sleeper to use
         * @return the builder instance for chaining
         * @throws NullPointerException if sleeper is null
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
