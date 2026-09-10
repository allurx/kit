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
package io.allurx.kit.base.function;

import io.allurx.kit.base.Conditional;
import io.allurx.kit.base.reflection.TypeConverter;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Adapts a supplied value to common result containers or an unchecked target type.
 * Each adapter calls {@link #get()} exactly once when invoked; adapters do not cache results
 * or defer evaluation until the returned container is consumed. Supplier failures propagate unchanged.
 *
 * @param <T> the type of result supplied by this provider
 * @author allurx
 */
public interface MultiOutputSupplier<T> extends Supplier<T> {

    /**
     * Evaluates the supplier and wraps a non-null result in an {@link Optional}.
     *
     * @return an empty optional for null, otherwise an optional containing the result
     */
    default Optional<T> getAsOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Evaluates the supplier and starts a fresh conditional chain with its result.
     *
     * @return a new conditional whose input is the result, including null
     */
    default Conditional<T> getAsConditional() {
        return Conditional.of(get());
    }

    /**
     * Evaluates this supplier once and returns the result wrapped in a {@link Stream}.
     * <p>
     * If the result is null, an empty stream is returned.
     *
     * @return an empty stream for null, otherwise a single-element stream
     */
    default Stream<T> getAsStream() {
        return Stream.ofNullable(get());
    }

    /**
     * Evaluates this supplier once and returns its result with an
     * {@linkplain TypeConverter#uncheckedCast(Object) unchecked cast}.
     * The caller must ensure compatibility with {@code R}; generic type arguments are not validated.
     *
     * @param <R> the target type
     * @return the supplied result as the target type
     * @see TypeConverter#uncheckedCast(Object)
     */
    default <R> R getAsType() {
        return TypeConverter.uncheckedCast(get());
    }

}
