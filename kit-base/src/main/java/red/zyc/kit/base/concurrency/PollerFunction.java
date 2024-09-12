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

/**
 * Interface for functions used in polling operations.
 *
 * <p>Defines the contract for functions that can be used by a {@link Poller}. Implementations must provide the actual execution logic.</p>
 *
 * @param <A> the type of the input to the function
 * @param <B> the type of the output of the function
 * @author allurx
 * @see RunnableFunction
 * @see CallableFunction
 */
public sealed interface PollerFunction<A, B> permits RunnableFunction, CallableFunction {

    /**
     * Executes the function with the given input.
     *
     * <p>Implementations must provide the specific logic for this method.</p>
     *
     * @param input the input for the function
     * @return the output of the function
     * @throws IllegalStateException if the method is not properly implemented
     */
    default B execute(A input) {
        throw new IllegalStateException("%s must implement this method to perform the actual function".formatted(getClass().getName()));
    }
}
