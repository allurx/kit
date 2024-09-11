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
package red.zyc.kit.base.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Utility class for capturing and storing generic types.
 * <p>
 * In Java, generic types are erased at runtime due to type erasure. To access generic types at runtime,
 * an anonymous subclass of {@code TypeToken} can be created to capture the generic type at compile time
 * and retain it at runtime.
 * </p>
 * <pre>
 *    {@code
 *      new TypeToken<List<String>>() {}.getCapturedType() => java.util.List<java.lang.String>
 *    }
 * </pre>
 *
 * @param <T> The generic type to be captured
 * @author allurx
 */
public abstract class TypeToken<T> {

    /**
     * The generic type {@link T} captured at compile time for runtime access.
     */
    private final Type capturedType;

    /**
     * When an anonymous subclass is created, if the parent class constructor is not explicitly called,
     * the no-argument constructor is automatically invoked. Through {@link Class#getGenericSuperclass()},
     * the generic type of {@code T} can be retrieved at compile time.
     */
    protected TypeToken() {
        capturedType = capture();
    }

    private TypeToken(Type type) {
        this.capturedType = type;
    }

    /**
     * Instantiates a {@link TypeToken} from a known {@link Type}.
     *
     * @param type The known {@link Type}, usually provided by {@link GenericDeclaration}.
     * @param <T>  Type inferred by the compiler.
     * @return A {@link TypeToken} instance.
     */
    public static <T> TypeToken<T> of(Type type) {
        return new TypeToken<>(type) {
        };
    }

    /**
     * Retrieves the generic type {@link T} captured at compile time.
     *
     * @return The generic type {@link T}.
     */
    public final Type getType() {
        return capturedType;
    }

    /**
     * Returns the raw {@link Class} represented by {@link #capturedType}.
     * <ul>
     * <li>If {@link T} is a non-generic {@link Class}, e.g., {@code String}, it returns the class itself.</li>
     * <li>If {@link T} is a {@link ParameterizedType}, e.g., {@code List<String>}, it returns {@code List.class}.</li>
     * <li>If {@link T} is a {@link TypeVariable}, e.g., {@code T}, returning {@code Object.class} is safe,
     *     as it may have multiple upper or lower bounds.</li>
     * <li>If {@link T} is a {@link GenericArrayType}, e.g., {@code List<String>[]}, it returns {@code List[].class}.</li>
     * <li>If {@link T} is a {@link WildcardType}, e.g., {@code ?}, an exception is thrown, as wildcard types
     *     cannot be directly instantiated and should not occur in this context.</li>
     * </ul>
     *
     * <p>This method helps avoid <b>unchecked</b> warnings from the compiler.</p>
     * <pre>
     *     {@code
     *
     *          // Some object
     *          var o = ...
     *
     *          // The compiler will issue an Unchecked cast warning
     *          @SuppressWarnings("unchecked")
     *          List<String> list1 = (List<String>) o;
     *
     *          // To avoid the Unchecked cast warning, ensure that 'o' is indeed a List,
     *          // otherwise, a ClassCastException will be thrown at runtime.
     *          List<String> list2 = new TypeToken<List<String>>() {}.getRawClass().cast(o);
     *     }
     * </pre>
     *
     * @return The raw {@link Class} represented by {@link #capturedType}.
     */
    public final Class<T> getRawClass() {
        var clazz = switch (capturedType) {
            case Class<?> c -> c;
            case ParameterizedType parameterizedType -> (Class<?>) parameterizedType.getRawType();
            case TypeVariable<?> ignored -> Object.class;
            case GenericArrayType genericArrayType ->
                    Array.newInstance(of(genericArrayType.getGenericComponentType()).getRawClass(), 0).getClass();
            case WildcardType wildcardType -> throw new IllegalStateException("Unexpected value: " + wildcardType);
            default -> throw new IllegalStateException("Unexpected value: " + capturedType);
        };
        @SuppressWarnings("unchecked")
        var rawType = (Class<T>) clazz;
        return rawType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TypeToken<?> target && capturedType.equals(target.capturedType);
    }

    @Override
    public int hashCode() {
        return capturedType.hashCode();
    }

    @Override
    public String toString() {
        return "TypeToken{capturedType=%s}".formatted(capturedType);
    }

    /**
     * Captures the generic type at compile time via an anonymous subclass.
     *
     * @return The generic type {@link T}.
     */
    private Type capture() {
        Class<?> clazz = getClass();
        Type superclass = clazz.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("%s must be a parameterized type".formatted(superclass));
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
}
