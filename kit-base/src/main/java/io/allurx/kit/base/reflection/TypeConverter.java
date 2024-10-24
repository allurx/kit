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
package io.allurx.kit.base.reflection;

/**
 * A utility class for various type conversion operations.
 * <p>
 * This class provides static methods for type casting and other related utilities.
 * </p>
 *
 * @author allurx
 */
public final class TypeConverter {

    private TypeConverter() {
    }

    /**
     * Performs an unchecked cast from one type to another.
     * <p>
     * This method allows casting an object of type A to type B without
     * compile-time type checking. Use with caution, as it may result in
     * {@link ClassCastException} at runtime if the object cannot be cast to the target type.
     * </p>
     *
     * @param a   the object to be cast
     * @param <A> the type of the object being cast
     * @param <B> the target type to cast to
     * @return the object cast to the target type B
     * @throws ClassCastException if the object cannot be cast to the target type at runtime
     */
    @SuppressWarnings("unchecked")
    public static <A, B> B uncheckedCast(A a) {
        return (B) a;
    }
}

