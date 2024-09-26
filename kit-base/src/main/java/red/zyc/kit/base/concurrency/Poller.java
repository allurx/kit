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

import red.zyc.kit.base.function.MultiOutputSupplier;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Poller interface defines a polling mechanism that allows repeated execution
 * based on a given input, processing function, and termination condition.
 *
 * @author allurx
 */
public interface Poller {

    /**
     * Executes a polling operation with customizable input, function, and termination condition.
     *
     * @param <A>       the input type
     * @param <B>       the result type
     * @param supplier  the supplier that provides the input for each iteration
     * @param function  the function applied to each input to generate a result
     * @param predicate the condition that, when true, will terminate the polling
     * @return a {@link PollResult} containing the count of iterations and the final result
     */
    <A, B> PollResult<B> poll(Supplier<A> supplier,
                              Function<? super A, ? extends B> function,
                              Predicate<? super B> predicate);

    /**
     * Polls the specified {@link Runnable} until the {@link BooleanSupplier} returns {@code true}.
     *
     * @param runnable        the operation to execute during each polling iteration
     * @param booleanSupplier the condition used to terminate the polling; polling stops when {@code true} is returned
     */
    default void poll(Runnable runnable, BooleanSupplier booleanSupplier) {
        poll(() -> null, unused -> {
            runnable.run();
            return null;
        }, unused -> booleanSupplier.getAsBoolean());
    }

    /**
     * PollResult holds the result of a polling operation, including the number of polling attempts
     * and the final outcome.
     *
     * @param count  the number of polling attempts
     * @param result the final result of the polling
     * @param <T>    the type of the polling result
     */
    record PollResult<T>(int count, T result) implements MultiOutputSupplier<T> {

        @Override
        public T get() {
            return result;
        }
    }

}
