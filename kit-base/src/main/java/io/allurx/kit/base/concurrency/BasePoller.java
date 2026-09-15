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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.allurx.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * Shares exception handling for synchronous pollers.
 * Configured exception classes match subclasses as well, including {@link Error} types if explicitly selected.
 * Ignored failures are logged by class name at {@link System.Logger.Level#WARNING} and become null results;
 * unmatched failures propagate unchanged. Predicate, clock, sleeper and logger failures are outside this handler.
 *
 * @author allurx
 */
public abstract class BasePoller implements Poller {

    /**
     * Logger used for ignored failures; messages contain the exception class name, not its stack trace.
     */
    protected final System.Logger logger;

    /**
     * Immutable snapshot of exception types ignored when retrieving or converting polling input.
     * Exceptions from the termination predicate are not ignored.
     */
    protected final List<Class<? extends Throwable>> ignoredExceptions;

    /**
     * Creates a poller with an immutable exception configuration.
     * A null list means no ignored exceptions; a null logger selects one named after the runtime class.
     * The exception list is copied so later changes by the caller cannot affect this poller.
     *
     * @param ignoredExceptions the exception types to ignore, or null for none
     * @param logger            the logger for ignored failures, or null for the default
     * @throws NullPointerException if the exception list contains a null element
     */
    protected BasePoller(List<Class<? extends Throwable>> ignoredExceptions, System.Logger logger) {
        this.logger = Optional.ofNullable(logger).orElse(System.getLogger(getClass().getName()));
        this.ignoredExceptions = ignoredExceptions == null ? List.of() : List.copyOf(ignoredExceptions);
    }

    /**
     * Validates that the provided function and predicate are not null.
     *
     * @param function  the function to be applied during polling
     * @param predicate the condition that determines when polling should stop
     * @param <A>       the input type of the function
     * @param <B>       the output type of the function
     * @throws NullPointerException if function or predicate is null
     */
    protected <A, B> void check(Function<? super A, ? extends B> function, Predicate<? super B> predicate) {
        Objects.requireNonNull(function, "The Function cannot be null");
        Objects.requireNonNull(predicate, "The Predicate used to test the output of the Function cannot be null");
    }

    /**
     * Retrieves input and applies the function, handling configured exceptions from either stage.
     * An ignored failure produces null, which the caller still passes to the termination predicate.
     *
     * @param supplier the supplier that retrieves input for this attempt
     * @param function the function to execute; subclasses should validate it with {@link #check(Function, Predicate)}
     * @param <A>      the input type of the function
     * @param <B>      the output type of the function
     * @return the result of the function execution, or null if an ignored exception was thrown
     * @throws NullPointerException if the supplier is null, regardless of the ignored exception types
     */
    protected <A, B> B execute(Supplier<? extends A> supplier, Function<? super A, ? extends B> function) {
        Objects.requireNonNull(supplier, "The Supplier cannot be null");
        try {
            return function.apply(supplier.get());
        } catch (Throwable t) {
            if (ignoredExceptions.stream().noneMatch(ignoredException -> ignoredException.isInstance(t))) throw t;
            logger.log(System.Logger.Level.WARNING, "Poller is ignoring the exception: {0}", t.getClass().getName());
            return null;
        }
    }

    /**
     * Mutable exception and logger configuration shared by poller builders.
     * Builders are not thread-safe. Building a poller snapshots exception types;
     * subsequent builder changes do not reconfigure an existing poller.
     *
     * @param <B> the builder type, allowing method chaining in subclasses
     * @author allurx
     */
    public static abstract class BasePollerBuilder<B extends BasePollerBuilder<B>> {

        /**
         * Creates a builder with no ignored exceptions and the poller's default logger.
         */
        protected BasePollerBuilder() {
        }

        /**
         * Configured logger, or null to use the poller's runtime class name.
         */
        protected System.Logger logger;

        /**
         * Configured exception types, or null to ignore no exceptions.
         */
        protected List<Class<? extends Throwable>> ignoredExceptions;

        /**
         * Replaces the exception types to ignore when retrieving or converting polling input.
         * Copies the array so subsequent changes to it do not affect this builder or its pollers.
         * An ignored failure produces null for the termination predicate, which must handle that value.
         * Exceptions thrown by the predicate are always propagated.
         *
         * @param ignoredExceptions the array of exception classes to ignore
         * @return the builder instance for chaining
         * @throws NullPointerException if the array or any of its elements is null
         */
        @SafeVarargs
        public final B ignoreExceptions(Class<? extends Throwable>... ignoredExceptions) {
            this.ignoredExceptions = List.of(Objects.requireNonNull(ignoredExceptions, "The Array of ignored exceptions cannot be null"));
            return uncheckedCast(this);
        }

        /**
         * Replaces the exception types to ignore when retrieving or converting polling input.
         * Copies the list so subsequent changes to it do not affect this builder or its pollers.
         * An ignored failure produces null for the termination predicate, which must handle that value.
         * Exceptions thrown by the predicate are always propagated.
         *
         * @param ignoredExceptions the list of exception classes to ignore
         * @return the builder instance for chaining
         * @throws NullPointerException if the list or any of its elements is null
         */
        public B ignoreExceptions(List<Class<? extends Throwable>> ignoredExceptions) {
            this.ignoredExceptions = List.copyOf(Objects.requireNonNull(ignoredExceptions, "The List of Ignore Exceptions cannot be null"));
            return uncheckedCast(this);
        }

        /**
         * Sets the logger to be used for logging during polling execution.
         *
         * @param logger the custom logger
         * @return the builder instance for chaining
         * @throws NullPointerException if logger is null
         */
        public B logger(System.Logger logger) {
            this.logger = Objects.requireNonNull(logger, "The Logger cannot be null");
            return uncheckedCast(this);
        }
    }

}
