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
package red.zyc.kit.base.poller;

/**
 * Represents a function that accepts an input but does not produce an output.
 *
 * <p>This interface extends {@link PollerFunction} with a type parameter of {@link Void} for the output. It defines a method {@link #run} that performs an operation using the input.</p>
 *
 * @param <T> the type of the input to the function
 * @author allurx
 */
@FunctionalInterface
public non-sealed interface RunnableFunction<T> extends PollerFunction<T, Void> {

    /**
     * Applies the input to the function.
     *
     * @param input the input to be processed
     */
    void run(T input);

    @Override
    default Void execute(T input) {
        run(input);
        return null;
    }

}
