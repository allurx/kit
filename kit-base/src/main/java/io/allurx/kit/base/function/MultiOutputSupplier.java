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
 * An interface that extends {@link Supplier} to provide multiple types of results.
 *
 * @param <T> the type of result supplied by this provider
 * @author allurx
 */
public interface MultiOutputSupplier<T> extends Supplier<T> {

    /**
     * Returns the result wrapped in an {@link Optional}.
     *
     * @return the result wrapped in an {@link Optional}.
     */
    default Optional<T> getAsOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Returns the result wrapped in an {@link Conditional}.
     *
     * @return the result wrapped in an {@link Conditional}.
     */
    default Conditional<T> getAsConditional() {
        return Conditional.of(get());
    }

    /**
     * Returns the result wrapped in an {@link Stream}.
     * <p>
     * If the result is null, an empty stream is returned.
     *
     * @return the result wrapped in an {@link Stream}.
     */
    default Stream<T> getAsStream() {
        return get() == null ? Stream.empty() : Stream.of(get());
    }

    /**
     * Casts the result to a specific type.
     * <p>
     * Note: This method uses unchecked casting and should be used with caution. It can also assist in type inference by the compiler.
     *
     * @param <R> the type to cast the result to
     * @return the result cast to the specified type
     * @throws ClassCastException if the result cannot be cast to the specified type
     */
    default <R> R getAsType() {
        return TypeConverter.uncheckedCast(get());
    }

}
