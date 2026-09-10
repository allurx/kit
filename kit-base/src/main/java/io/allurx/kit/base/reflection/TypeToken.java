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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Objects;

/**
 * Captures a declared type through a direct subclass, or stores an explicit type.
 * Type variables are preserved without resolving call-site type arguments.
 * <pre>{@code new TypeToken<List<String>>() {}.getType()}</pre>
 *
 * <p>Capturing {@code T} inside a generic method preserves that method's type variable,
 * even when the caller supplies a concrete type. Tokens describe types; they do not verify
 * that a runtime value satisfies generic arguments. Explicit types are retained by reference.
 *
 * @param <T> the represented compile-time type
 * @author allurx
 * @see Type
 * @see Class
 * @see ParameterizedType
 * @see GenericArrayType
 * @see TypeVariable
 * @see WildcardType
 */
public abstract class TypeToken<T> {

    private final Type capturedType;

    /**
     * Captures the type argument of a direct {@code TypeToken} or {@link AnnotatedTypeToken} subclass.
     * Other inheritance structures must use {@link #TypeToken(Type)}.
     *
     * @throws IllegalArgumentException if the runtime class is not a direct, parameterized subclass
     */
    protected TypeToken() {
        this.capturedType = capture();
    }

    /**
     * Stores an explicit type without inspecting the subclass hierarchy.
     * Subclasses are responsible for ensuring that the supplied type agrees with {@code T}.
     *
     * @param type the type
     * @throws NullPointerException if type is null
     */
    protected TypeToken(Type type) {
        this.capturedType = Objects.requireNonNull(type, "type");
    }

    /**
     * Creates a token whose compile-time type is determined by the supplied class.
     *
     * @param type the class represented by the token
     * @param <T> the type represented by the class
     * @return a token bound to the supplied class's type
     * @throws NullPointerException if type is null
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        return new TypeToken<>(type) {
        };
    }

    /**
     * Creates a token from a type known only at runtime.
     * A reflective {@link Type} does not bind a compile-time type parameter, so the result uses a wildcard.
     *
     * @param type the runtime type to capture
     * @return a token preserving the supplied type without claiming a specific compile-time type
     * @throws NullPointerException if type is null
     */
    public static TypeToken<?> of(Type type) {
        return new TypeToken<>(type) {
        };
    }

    /**
     * Returns the captured or explicitly supplied type.
     *
     * @return the non-null captured type, including unresolved type variables
     */
    public final Type getType() {
        return capturedType;
    }

    /**
     * Returns the raw class, resolving generic arrays through their component types.
     * Type variables and wildcards use the raw class of their first upper bound.
     * <p>Generic arguments are erased: {@code List<String>} yields {@code List.class}.
     * {@link Class#cast(Object)} checks only this raw class, not generic arguments.
     *
     * @return the raw class, which may represent a supertype of {@code T}
     * @throws IllegalArgumentException if the captured type or a recursively inspected component or bound
     *                                  is not a supported reflection type
     */
    public final Class<? super T> getRawClass() {
        var clazz = switch (capturedType) {
            case Class<?> c -> c;
            case ParameterizedType parameterizedType -> (Class<?>) parameterizedType.getRawType();
            case TypeVariable<?> typeVariable -> of(typeVariable.getBounds()[0]).getRawClass();
            case GenericArrayType genericArrayType ->
                    Array.newInstance(of(genericArrayType.getGenericComponentType()).getRawClass(), 0).getClass();
            case WildcardType wildcardType -> of(wildcardType.getUpperBounds()[0]).getRawClass();
            default -> throw new IllegalArgumentException("Unexpected type: %s".formatted(capturedType));
        };
        return TypeConverter.uncheckedCast(clazz);
    }

    /**
     * Compares captured types when the other token permits comparison with this token.
     * Different anonymous subclasses can be equal.
     * Equality delegates to the captured {@link Type}; annotated tokens use their own equality policy.
     *
     * @param o the object to compare
     * @return whether both tokens have compatible equality semantics and equal captured types
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof TypeToken<?> target && target.canEqual(this) && capturedType.equals(target.capturedType);
    }

    /**
     * Determines whether another object may be equal to this token.
     * Subclasses that include additional state in equality must override this method together with
     * {@link #equals(Object)} and {@link #hashCode()} to preserve symmetry.
     *
     * @param o the other object
     * @return whether the object can participate in this token's equality comparison
     */
    protected boolean canEqual(Object o) {
        return o instanceof TypeToken<?>;
    }

    /**
     * Returns the captured type's hash code, consistent with type-based equality.
     *
     * @return the captured type's hash code
     */
    @Override
    public int hashCode() {
        return capturedType.hashCode();
    }

    /**
     * Returns a diagnostic description of the captured type.
     *
     * @return a description intended for display, not persistence or parsing
     */
    @Override
    public String toString() {
        return "TypeToken{capturedType=%s}".formatted(capturedType);
    }

    private Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterizedType)
                || (parameterizedType.getRawType() != TypeToken.class
                && parameterizedType.getRawType() != AnnotatedTypeToken.class)) {
            throw new IllegalArgumentException(
                    "%s must directly extend TypeToken<T> or AnnotatedTypeToken<T>, or pass an explicit type"
                            .formatted(getClass().getName()));
        }
        return parameterizedType.getActualTypeArguments()[0];
    }

}
