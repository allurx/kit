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
package red.zyc.kit.base.concurrency;

import red.zyc.kit.base.function.ThrowableSupplier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Executes the {@link #function} at a fixed {@link #interval} within the {@link Clock#instant()}+{@link #duration} period until any of the following conditions is met:
 * <ul>
 *     <li>{@link #predicate} returns true for the output of {@link #function}</li>
 *     <li>The timeout occurs (the polling duration exceeds {@link Clock#instant()}+{@link #duration})</li>
 * </ul>
 * <p>Here are two common examples of usage:</p>
 * <p style="color: #4682B4;font-weight: bold;">
 * Print the current date and time every 500 milliseconds for up to 10 seconds. When the timeout expires, a {@link RuntimeException} will be thrown.
 * </p>
 * <pre>
 * {@code
 *     Poller.<Void, Void>builder()
 *              .timing(Duration.ofSeconds(10), Duration.ofMillis(500))
 *              .<RunnableFunction<Void>>execute(null, o -> System.out.println(LocalDateTime.now()))
 *              .predicate(o -> false)
 *              .onTimeout(throwingRunnable(() -> new RuntimeException("Timeout")))
 *              .build()
 *              .poll();
 * }
 * </pre>
 * <p style="color: #4682B4;font-weight: bold;">
 * Print `num++` every 500 milliseconds for up to 10 seconds until `num` equals 12, then exit the polling.
 * </p>
 * <pre>
 * {@code
 *         AtomicInteger num = new AtomicInteger(1);
 *         Poller.<AtomicInteger, Integer>builder()
 *                 .timing(Duration.ofSeconds(10), Duration.ofMillis(500))
 *                 .<CallableFunction<AtomicInteger, Integer>>execute(num, i -> {
 *                     System.out.println(num.get());
 *                     return num.getAndIncrement();
 *                 })
 *                 .predicate(o -> o == 12)
 *                 .onTimeout(throwingRunnable(() -> new RuntimeException("Timeout")))
 *                 .build()
 *                 .poll();
 * }
 * </pre>
 *
 * @param <A> the input type of the polling function
 * @param <B> the output type of the polling function
 * @author allurx
 */
public class Poller<A, B> {

    private static final System.Logger LOGGER = System.getLogger(Poller.class.getName());

    private Poller() {
    }

    /**
     * The {@link Clock} used for timing
     */
    private Clock clock;

    /**
     * The total duration for polling
     */
    private Duration duration;

    /**
     * The interval between each poll
     */
    private Duration interval;

    /**
     * The function to be executed during polling
     */
    private PollerFunction<A, B> function;

    /**
     * The input for {@link #function}
     */
    private A input;

    /**
     * A predicate to determine if polling should stop based on the {@link #function}'s output
     */
    private Predicate<B> predicate;

    /**
     * The {@link Sleeper} used for pausing between polls
     */
    private Sleeper sleeper;

    /**
     * The action to be performed when a timeout occurs
     */
    private Runnable timeoutAction;

    /**
     * The list of exceptions to ignore during {@link #function} execution
     */
    private final List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();

    /**
     * Starts the polling process and returns the result.
     *
     * @return an {@link Optional} containing the output of {@link #function}, or empty if polling did not succeed
     */
    public Optional<B> poll() {
        return polling();
    }

    /**
     * Encapsulates the result of a polling attempt.
     */
    private class PollResult {

        /**
         * The number of polling attempts
         */
        private int num = 0;

        /**
         * The output of the function
         */
        private B output = null;

        /**
         * Indicates if polling has timed out
         */
        private boolean timeout = false;

        /**
         * Indicates if the function execution was successful
         */
        private boolean success = false;
    }

    /**
     * Performs the polling process
     */
    private Optional<B> polling() {
        PollResult result = new PollResult();
        Instant endInstant = clock.instant().plus(duration);
        for (; ; ) {

            // Execute the function
            execute(result);

            // Exit polling if the function execution was successful
            if (result.success) break;

            // Check if polling needs to stop based on the remaining time
            result.timeout = clock.instant().plus(interval).isAfter(endInstant);
            if (result.timeout) {
                timeoutAction.run();
                break;
            }

            // Pause before the next polling attempt
            sleeper.sleep(interval);
        }
        return Optional.ofNullable(result.output);
    }

    /**
     * Executes the {@link #function} and handles exceptions.
     * If an exception occurs that is not in {@link #ignoredExceptions}, it is thrown.
     *
     * @param result The {@link PollResult} to update with the execution result
     */
    private void execute(PollResult result) {
        try {
            result.num++;
            result.output = function.execute(input);
            result.success = predicate.test(result.output);
        } catch (Throwable t) {
            if (ignoredExceptions.stream().noneMatch(ignoredException -> ignoredException.isInstance(t))) throw t;
            LOGGER.log(System.Logger.Level.WARNING, "Poller is ignoring the exception: {0}", t.getClass().getName());
        }
    }

    /**
     * Creates a {@link Runnable} that throws a {@link RuntimeException} provided by the given {@link ThrowableSupplier}.
     *
     * @param throwableSupplier a {@link ThrowableSupplier} that provides the {@link RuntimeException} to be thrown
     * @return a {@link Runnable} that throws the exception when executed
     */
    public static Runnable throwingRunnable(ThrowableSupplier<? extends RuntimeException> throwableSupplier) {
        return () -> {
            throw throwableSupplier.get();
        };
    }

    // Builder methods

    /**
     * Creates a new {@link Builder} instance for constructing a {@link Poller}.
     *
     * @param <A> the input type of the {@link #function}
     * @param <B> the output type of the {@link #function}
     * @return a new {@link Builder} instance for constructing a {@link Poller}
     */
    public static <A, B> Builder<A, B> builder() {
        return new Builder<>(new Poller<>());
    }

    /**
     * Builder for creating a {@link Poller} instance.
     *
     * @param <A> the input type of the {@link #function}
     * @param <B> the output type of the {@link #function}
     */
    public static class Builder<A, B> {

        private final Poller<A, B> poller;

        /**
         * Constructs a {@link Builder} with the specified {@link Poller}.
         *
         * @param poller the {@link Poller} instance to be used by the builder
         */
        public Builder(Poller<A, B> poller) {
            this.poller = poller;
        }

        /**
         * Configures the timing parameters for the {@link Poller} using the default system clock.
         *
         * @param duration {@link Poller#duration}
         * @param interval {@link Poller#interval}
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> timing(Duration duration, Duration interval) {
            return timing(Clock.systemDefaultZone(), duration, interval);
        }

        /**
         * Configures the timing parameters for the {@link Poller} using the specified {@link Clock}.
         *
         * @param clock    {@link Poller#clock}
         * @param duration {@link Poller#duration}
         * @param interval {@link Poller#interval}
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> timing(Clock clock, Duration duration, Duration interval) {
            poller.clock = clock;
            poller.duration = duration;
            poller.interval = interval;
            return this;
        }

        /**
         * Sets the input and function to be used by the {@link Poller}.
         *
         * @param <F>      the type of {@link PollerFunction}
         * @param input    {@link Poller#input}
         * @param function {@link Poller#function}
         * @return the {@link Builder} instance for method chaining
         */
        public <F extends PollerFunction<A, B>> Builder<A, B> execute(A input, F function) {
            poller.input = input;
            poller.function = function;
            return this;
        }

        /**
         * Sets the predicate to determine if polling should stop.
         *
         * @param predicate {@link Poller#predicate}
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> until(Predicate<B> predicate) {
            poller.predicate = predicate;
            return this;
        }

        /**
         * Sets the {@link Sleeper} to use for pausing between polls.
         *
         * @param sleeper {@link Poller#sleeper}
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> sleeper(Sleeper sleeper) {
            poller.sleeper = sleeper;
            return this;
        }

        /**
         * Sets the action to be performed when polling times out.
         *
         * @param timeoutAction {@link Poller#timeoutAction}
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> onTimeout(Runnable timeoutAction) {
            poller.timeoutAction = timeoutAction;
            return this;
        }

        /**
         * Adds an exception to be ignored during polling.
         *
         * @param ignoredException the class of the exception to be ignored
         * @return the {@link Builder} instance for method chaining
         */
        public Builder<A, B> ignoreExceptions(Class<? extends Throwable> ignoredException) {
            poller.ignoredExceptions.add(ignoredException);
            return this;
        }

        /**
         * Builds and returns a {@link Poller} instance with the configured settings.
         *
         * @return a new {@link Poller} instance
         */
        public Poller<A, B> build() {
            if (poller.clock == null) poller.clock = Clock.systemDefaultZone();
            if (poller.duration == null) poller.duration = Duration.ZERO;
            if (poller.interval == null) poller.interval = Duration.ZERO;
            if (poller.function == null) poller.function = (CallableFunction<A, B>) o -> null;
            if (poller.predicate == null) poller.predicate = b -> true;
            if (poller.sleeper == null) poller.sleeper = Sleeper.DEFAULT;
            if (poller.timeoutAction == null) poller.timeoutAction = () -> {
            };
            return poller;
        }
    }
}
