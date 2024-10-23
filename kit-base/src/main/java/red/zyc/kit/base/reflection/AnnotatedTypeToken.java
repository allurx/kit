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

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Captures annotations on various types at runtime, including generic parameters, arrays,
 * type variables, and wildcards.
 * <p>
 * This class allows the creation of an anonymous subclass to retrieve annotations on different
 * types at runtime, provided that the annotations' retention policy is set to
 * {@link RetentionPolicy#RUNTIME}.
 * </p>
 * <pre>
 *    {@code
 *      var annotatedTypeToken = new AnnotatedTypeToken<List<@MyAnnotation String>>() {};
 *      var annotatedType = annotatedTypeToken.getAnnotatedType();
 *      // Retrieves @MyAnnotation from the annotatedType for the generic type parameter.
 *    }
 * </pre>
 *
 * @param <T> The type whose annotations are captured
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
     * Captures the {@link AnnotatedType} of {@link T}.
     * Currently, due to Java restrictions, {@link AnnotatedType#getType()} cannot be
     * referenced before calling the superclass constructor. This may change with future
     * Java language updates (e.g., JEP 447).
     */
    protected AnnotatedTypeToken() {
        this.annotatedType = capture();
    }

    /**
     * Constructs an {@code AnnotatedTypeToken} from a given {@link AnnotatedType}.
     *
     * @param annotatedType the annotated type
     */
    protected AnnotatedTypeToken(AnnotatedType annotatedType) {
        super(annotatedType.getType());
        this.annotatedType = annotatedType;
    }

    /**
     * Creates an {@link AnnotatedTypeToken} for the specified annotated type.
     *
     * @param annotatedType the annotated type to capture
     * @param <T>           the type to capture
     * @return a new {@link AnnotatedTypeToken} instance
     */
    public static <T> AnnotatedTypeToken<T> of(AnnotatedType annotatedType) {
        return new AnnotatedTypeToken<>(annotatedType) {
        };
    }

    /**
     * Returns the captured {@link AnnotatedType}.
     *
     * @return the annotated type of {@link T}
     */
    public final AnnotatedType getAnnotatedType() {
        return annotatedType;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AnnotatedTypeToken<?> target && annotatedType.equals(target.annotatedType);
    }

    @Override
    public int hashCode() {
        return annotatedType.hashCode();
    }

    @Override
    public String toString() {
        return "AnnotatedTypeToken{annotatedType=%s}".formatted(annotatedType);
    }

    /**
     * Captures the {@link AnnotatedType} of the generic type {@link T}.
     *
     * @return the captured annotated type
     * @throws IllegalArgumentException if the type is not parameterized
     */
    private AnnotatedType capture() {
        Class<?> clazz = getClass();
        Type superclass = clazz.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("%s must be a parameterized type".formatted(superclass));
        }
        return ((AnnotatedParameterizedType) clazz.getAnnotatedSuperclass()).getAnnotatedActualTypeArguments()[0];
    }
}
