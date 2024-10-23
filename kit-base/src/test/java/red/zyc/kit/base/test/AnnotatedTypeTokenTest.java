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
package red.zyc.kit.base.test;

import org.junit.jupiter.api.Test;
import red.zyc.kit.base.reflection.AnnotatedTypeToken;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests for {@link AnnotatedTypeToken} capturing annotations on different types.
 *
 * @author allurx
 */
class AnnotatedTypeTokenTest {

    @Target({ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnnotation {
        int value();
    }

    /**
     * Tests capturing annotations on a simple class type.
     *
     * @see sun.reflect.annotation.AnnotatedTypeFactory.AnnotatedTypeBaseImpl
     */
    @Test
    void testCaptureAnnotationOnClass() {
        var annotatedTypeToken = new AnnotatedTypeToken<@MyAnnotation(1) String>() {
        };

        var annotatedType = annotatedTypeToken.getAnnotatedType();
        var myAnnotation = annotatedType.getDeclaredAnnotation(MyAnnotation.class);

        // assertInstanceOf(sun.reflect.annotation.AnnotatedTypeFactory.AnnotatedTypeBaseImpl.class, annotatedType);
        assertEquals(1, myAnnotation.value());
    }

    /**
     * Tests capturing annotations on a parameterized type with a class and a generic argument.
     *
     * @see AnnotatedParameterizedType
     */
    @Test
    void testCaptureAnnotationOnParameterizedType() {
        var annotatedTypeToken = new AnnotatedTypeToken<@MyAnnotation(1) List<@MyAnnotation(2) String>>() {
        };

        var annotatedType = assertInstanceOf(AnnotatedParameterizedType.class, annotatedTypeToken.getAnnotatedType());
        var myAnnotation1 = annotatedType.getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation2 = annotatedType.getAnnotatedActualTypeArguments()[0].getDeclaredAnnotation(MyAnnotation.class);

        assertEquals(1, myAnnotation1.value());
        assertEquals(2, myAnnotation2.value());
    }

    /**
     * Tests capturing annotations on a type variable and its bounds.
     *
     * @param <T> a generic type with annotations on its bounds, including Map and AutoCloseable
     * @see AnnotatedTypeVariable
     */
    @Test
    <T extends @MyAnnotation(2) Map<@MyAnnotation(3) Integer, @MyAnnotation(4) String> & @MyAnnotation(5) AutoCloseable>
    void testCaptureAnnotationOnTypeVariable() {
        var annotatedTypeToken = new AnnotatedTypeToken<@MyAnnotation(1) T>() {
        };

        var annotatedType = assertInstanceOf(AnnotatedTypeVariable.class, annotatedTypeToken.getAnnotatedType());

        // T's bounds: Map<Integer, String> & AutoCloseable
        var bounds = annotatedType.getAnnotatedBounds();
        var bound0 = assertInstanceOf(AnnotatedParameterizedType.class, bounds[0]);
        var bound1 = bounds[1];
        // assertInstanceOf(sun.reflect.annotation.AnnotatedTypeFactory.AnnotatedTypeBaseImpl.class, bound1);

        var myAnnotation1 = annotatedType.getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation2 = bound0.getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation3 = bound0.getAnnotatedActualTypeArguments()[0].getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation4 = bound0.getAnnotatedActualTypeArguments()[1].getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation5 = bound1.getDeclaredAnnotation(MyAnnotation.class);

        assertEquals(1, myAnnotation1.value());
        assertEquals(2, myAnnotation2.value());
        assertEquals(3, myAnnotation3.value());
        assertEquals(4, myAnnotation4.value());
        assertEquals(5, myAnnotation5.value());
    }

    /**
     * Tests capturing annotations on a wildcard type.
     *
     * @see AnnotatedWildcardType
     */
    @Test
    void testCaptureAnnotationOnWildcardType() {
        var annotatedTypeToken = new AnnotatedTypeToken<@MyAnnotation(1) List<@MyAnnotation(2) ?>>() {
        };

        var annotatedType = assertInstanceOf(AnnotatedParameterizedType.class, annotatedTypeToken.getAnnotatedType());
        var myAnnotation1 = annotatedType.getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation2 = assertInstanceOf(AnnotatedWildcardType.class, annotatedType.getAnnotatedActualTypeArguments()[0]).getDeclaredAnnotation(MyAnnotation.class);

        assertEquals(1, myAnnotation1.value());
        assertEquals(2, myAnnotation2.value());
    }

    /**
     * Tests capturing annotations on an array type.
     *
     * @see AnnotatedArrayType
     */
    @Test
    void testCaptureAnnotationOnArrayType() {
        var annotatedTypeToken = new AnnotatedTypeToken<@MyAnnotation(1) String @MyAnnotation(2) []>() {
        };

        var annotatedType = assertInstanceOf(AnnotatedArrayType.class, annotatedTypeToken.getAnnotatedType());
        var myAnnotation1 = annotatedType.getAnnotatedGenericComponentType().getDeclaredAnnotation(MyAnnotation.class);
        var myAnnotation2 = annotatedType.getDeclaredAnnotation(MyAnnotation.class);

        assertEquals(1, myAnnotation1.value());
        assertEquals(2, myAnnotation2.value());
    }

}


