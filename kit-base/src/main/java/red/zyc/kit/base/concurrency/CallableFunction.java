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

import java.util.function.Function;

/**
 * Represents a function that accepts an input and produces an output.
 *
 * <p>This interface extends {@link Function} and {@link PollerFunction}, providing a default implementation of {@link PollerFunction#execute} using the {@link Function#apply} method.</p>
 *
 * @param <A> the type of the input to the function
 * @param <B> the type of the output from the function
 * @author allurx
 * @see Function#apply
 * @see PollerFunction
 */
@FunctionalInterface
public non-sealed interface CallableFunction<A, B> extends Function<A, B>, PollerFunction<A, B> {

    @Override
    default B execute(A input) {
        return apply(input);
    }
}
