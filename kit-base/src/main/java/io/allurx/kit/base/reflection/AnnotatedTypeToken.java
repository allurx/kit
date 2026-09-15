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

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;

/**
 * Captures a declared type and its {@link RetentionPolicy#RUNTIME} annotations through a direct subclass.
 * Type variables and their annotated bounds are preserved without resolving call-site type arguments.
 * <pre>{@code new AnnotatedTypeToken<List<@MyAnnotation String>>() {}.getAnnotatedType()}</pre>
 *
 * <p>Annotations are available through {@link #getAnnotatedType()} and its nested annotated types.
 * Inherited {@link #getType()} and {@link #getRawClass()} expose the underlying type without annotations.
 * Equality and hashing delegate to the captured {@link AnnotatedType} implementation.
 *
 * @param <T> the type whose annotations are captured
 * @author allurx
 * @see AnnotatedType
 * @see ParameterizedType
 * @see AnnotatedArrayType
 * @see AnnotatedTypeVariable
 * @see AnnotatedWildcardType
 * @see RetentionPolicy
 */
public abstract class AnnotatedTypeToken<T> extends TypeToken<T> {

    private final AnnotatedType annotatedType;

    /**
     * Captures the annotated type argument of a direct subclass.
     * Other inheritance structures must use {@link #AnnotatedTypeToken(AnnotatedType)}.
     *
     * @throws IllegalArgumentException if the runtime class is not a direct, parameterized subclass
     */
    protected AnnotatedTypeToken() {
        // TypeToken validates the direct superclass before annotation capture.
        var superclass = (AnnotatedParameterizedType) getClass().getAnnotatedSuperclass();
        this.annotatedType = superclass.getAnnotatedActualTypeArguments()[0];
    }

    /**
     * Stores an explicit annotated type without inspecting the subclass hierarchy.
     * The value is retained by reference; subclasses must ensure its underlying type agrees with {@code T}.
     *
     * @param annotatedType the annotated type
     * @throws NullPointerException if annotatedType or its underlying type is null
     */
    protected AnnotatedTypeToken(AnnotatedType annotatedType) {
        super(annotatedType.getType());
        this.annotatedType = annotatedType;
    }

    /**
     * Creates a token from an annotated type known only at runtime.
     * An {@link AnnotatedType} does not bind a compile-time type parameter, so the result uses a wildcard.
     * Use an anonymous subclass to capture both a static generic type and its annotations.
     *
     * @param annotatedType the annotated type to capture
     * @return a token preserving the supplied type and annotations without claiming a specific compile-time type
     * @throws NullPointerException if annotatedType or its underlying type is null
     */
    public static AnnotatedTypeToken<?> of(AnnotatedType annotatedType) {
        return new AnnotatedTypeToken<>(annotatedType) {
        };
    }

    /**
     * Returns the captured {@link AnnotatedType}.
     *
     * @return the non-null annotated type, including annotations on generic arguments and bounds
     */
    public final AnnotatedType getAnnotatedType() {
        return annotatedType;
    }

    /**
     * Compares annotated types only with compatible annotated tokens.
     * Plain tokens remain unequal even when no type annotations are present.
     *
     * @param o the object to compare
     * @return whether both tokens have compatible equality semantics and equal annotated types
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AnnotatedTypeToken<?> target && target.canEqual(this) && annotatedType.equals(target.annotatedType);
    }

    /**
     * Accepts only annotated tokens because annotations participate in equality.
     *
     * @param o the other object
     * @return whether the object is an annotated token
     */
    @Override
    protected boolean canEqual(Object o) {
        return o instanceof AnnotatedTypeToken<?>;
    }

    /**
     * Returns the annotated type's hash code, consistent with annotated-type equality.
     *
     * @return the annotated type's hash code
     */
    @Override
    public int hashCode() {
        return annotatedType.hashCode();
    }

    /**
     * Returns a diagnostic description of the captured annotated type.
     *
     * @return a description intended for display, not persistence or parsing
     */
    @Override
    public String toString() {
        return "AnnotatedTypeToken{annotatedType=%s}".formatted(annotatedType);
    }

}
