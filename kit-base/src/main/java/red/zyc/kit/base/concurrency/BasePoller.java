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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static red.zyc.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * Abstract base class for creating a Poller with customizable exception handling and logging.
 * Provides the framework for executing a polling function and handling any ignored exceptions.
 *
 * @author allurx
 */
public abstract class BasePoller implements Poller {

    /**
     * Logger instance for recording events and errors during polling.
     */
    protected final System.Logger logger;

    /**
     * List of exceptions to be ignored during polling. Exceptions in this list will not terminate polling.
     */
    protected final List<Class<? extends Throwable>> ignoredExceptions;

    /**
     * Constructs a BasePoller with the provided list of ignored exceptions and a logger.
     * If no logger or exceptions are provided, defaults are used.
     *
     * @param ignoredExceptions the list of exceptions to ignore during polling
     * @param logger            the logger used to log events during polling
     */
    protected BasePoller(List<Class<? extends Throwable>> ignoredExceptions, System.Logger logger) {
        this.logger = Optional.ofNullable(logger).orElse(System.getLogger(getClass().getName()));
        this.ignoredExceptions = Optional.ofNullable(ignoredExceptions).orElse(new ArrayList<>());
    }

    /**
     * Validates that the provided function and predicate are not null.
     *
     * @param function  the function to be applied during polling
     * @param predicate the condition that determines when polling should stop
     * @param <A>       the input type of the function
     * @param <B>       the output type of the function
     */
    protected <A, B> void check(Function<? super A, ? extends B> function, Predicate<? super B> predicate) {
        Objects.requireNonNull(function, "The Function cannot be null");
        Objects.requireNonNull(predicate, "The Predicate used to test the output of the Function cannot be null");
    }

    /**
     * Executes the provided function and handles any ignored exceptions.
     *
     * @param input    the input provided to the function
     * @param function the function to be executed
     * @param <A>      the input type of the function
     * @param <B>      the output type of the function
     * @return the result of the function execution, or null if an ignored exception was thrown
     */
    protected <A, B> B execute(A input, Function<? super A, ? extends B> function) {
        try {
            return function.apply(input);
        } catch (Throwable t) {
            if (ignoredExceptions.stream().noneMatch(ignoredException -> ignoredException.isInstance(t))) throw t;
            logger.log(System.Logger.Level.WARNING, "Poller is ignoring the exception: {0}", t.getClass().getName());
            return null;
        }
    }

    /**
     * BasePollerBuilder provides a common foundation for building various types of Pollers.
     * It allows configuration of exceptions to ignore and custom logging.
     *
     * @param <B> the builder type, allowing method chaining in subclasses
     */
    public static class BasePollerBuilder<B extends BasePollerBuilder<B>> {

        /**
         * Default constructor
         */
        public BasePollerBuilder() {
        }

        /**
         * {@link BasePoller#logger}
         */
        protected System.Logger logger;

        /**
         * {@link BasePoller#ignoredExceptions}
         */
        protected List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();

        /**
         * Adds an exception type to the list of ignored exceptions. Poller will ignore these exceptions during execution.
         *
         * @param ignoredException the exception class to ignore
         * @return the builder instance for chaining
         */
        public B ignoreExceptions(Class<? extends Throwable> ignoredException) {
            this.ignoredExceptions.add(Objects.requireNonNull(ignoredException, "The Ignore Exception cannot be null"));
            return uncheckedCast(this);
        }

        /**
         * Sets the list of ignored exceptions. Poller will ignore all exceptions in this list during execution.
         *
         * @param ignoredExceptions the list of exception classes to ignore
         * @return the builder instance for chaining
         */
        public B ignoreExceptions(List<Class<? extends Throwable>> ignoredExceptions) {
            this.ignoredExceptions = Objects.requireNonNull(ignoredExceptions, "The List of Ignore Exceptions cannot be null");
            return uncheckedCast(this);
        }

        /**
         * Sets the logger to be used for logging during polling execution.
         *
         * @param logger the custom logger
         * @return the builder instance for chaining
         */
        public B logger(System.Logger logger) {
            this.logger = Objects.requireNonNull(logger, "The Logger cannot be null");
            return uncheckedCast(this);
        }
    }

}
