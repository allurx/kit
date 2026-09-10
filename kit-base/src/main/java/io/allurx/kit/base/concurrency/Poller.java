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

import io.allurx.kit.base.function.MultiOutputSupplier;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Runs repeated attempts on the calling thread, testing each result for completion.
 * An attempt retrieves fresh input, applies a function, then evaluates the termination predicate.
 * Implementations determine additional stopping rules such as attempt limits or deadlines.
 *
 * <p>A returned result does not establish that the predicate matched: a limit or interruption
 * may have ended polling. Callers that need to distinguish success must inspect the result.
 *
 * @author allurx
 */
public interface Poller {

    /**
     * Runs attempts until the predicate returns {@code true} or an implementation's limit is reached.
     * Input and function results may be null if the following callback accepts null.
     * Exception handling is implementation-specific; {@link BasePoller} supports configured
     * failures from input retrieval and conversion, but always propagates predicate failures.
     *
     * @param <A>       the input type
     * @param <B>       the result type
     * @param supplier  the supplier that provides the input for each iteration
     * @param function  the function applied to each input to generate a result
     * @param predicate the condition that, when true, will terminate the polling
     * @return the actual attempt count and the last result, which may be null
     * @throws NullPointerException if a required callback is null; validation timing depends on the implementation
     */
    <A, B> PollResult<B> poll(Supplier<? extends A> supplier,
                              Function<? super A, ? extends B> function,
                              Predicate<? super B> predicate);

    /**
     * Runs the action before testing the condition, stopping on a match or the implementation's limit.
     * Both arguments are validated before polling starts or either callback executes.
     * The action is adapted as the conversion function, so configured ignored exceptions apply to it.
     *
     * @param runnable        the operation to execute during each polling iteration
     * @param booleanSupplier the condition used to terminate the polling; polling stops when {@code true} is returned
     * @throws NullPointerException if runnable or booleanSupplier is null
     */
    default void poll(Runnable runnable, BooleanSupplier booleanSupplier) {
        Objects.requireNonNull(runnable, "The Runnable cannot be null");
        Objects.requireNonNull(booleanSupplier, "The BooleanSupplier cannot be null");
        poll(() -> null, unused -> {
            runnable.run();
            return null;
        }, unused -> booleanSupplier.getAsBoolean());
    }

    /**
     * Stores the number of attempts and the last value without recording why polling stopped.
     * An ignored failure counts as an attempt and replaces the last value with null.
     * If no attempt ran, the supplied pollers return zero and null.
     * The result value is retained by reference and is not copied.
     *
     * @param count  the number of polling attempts
     * @param result the last result, possibly null
     * @param <T>    the type of the polling result
     */
    record PollResult<T>(int count, T result) implements MultiOutputSupplier<T> {

        /**
         * Returns the stored result without repeating the polling operation.
         *
         * @return the same value as {@link #result()}
         */
        @Override
        public T get() {
            return result;
        }
    }

}
