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
package io.allurx.kit.base.test;

import io.allurx.kit.base.reflection.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies captured reflection types and raw classes, including unresolved variables and arrays.
 *
 * @author allurx
 */
class TypeTokenTest {

    /**
     * A direct class argument is preserved as both the captured type and raw class.
     */
    @Test
    void testCaptureClass() {
        var typeToken = new TypeToken<String>() {
        };
        var capturedType = typeToken.getType();
        var rawClass = typeToken.getRawClass();

        assertEquals(String.class, capturedType);
        assertEquals(String.class, rawClass);
    }

    /**
     * Without a target type, the diamond infers Object; reflection does not recover a more specific type.
     */
    @Test
    void testCaptureCompilerTypeInference() {
        var typeToken = new TypeToken<>() {
        };
        var capturedType = typeToken.getType();
        var rawClass = typeToken.getRawClass();

        assertEquals(Object.class, capturedType);
        assertEquals(Object.class, rawClass);
    }

    /**
     * Capturing preserves generic arguments while raw-class access erases them.
     */
    @Test
    void testCaptureParameterizedType() {
        var typeToken = new TypeToken<List<String>>() {
        };
        var capturedType = assertInstanceOf(ParameterizedType.class, typeToken.getType());

        var actualTypeArgument = capturedType.getActualTypeArguments()[0];
        assertEquals(String.class, actualTypeArgument);

        var rawClass = typeToken.getRawClass();
        assertEquals(List.class, rawClass);
    }

    /**
     * A type variable retains every bound; raw-class access resolves only its first upper bound.
     *
     * @param <T> a type variable with bounds
     * @see TypeVariable
     */
    @Test
    <T extends Map<Integer, String> & AutoCloseable> void testCaptureTypeVariable() {
        var typeToken = new TypeToken<T>() {
        };
        var capturedType = assertInstanceOf(TypeVariable.class, typeToken.getType());

        var bounds = capturedType.getBounds();

        var bound0 = assertInstanceOf(ParameterizedType.class, bounds[0]);
        var actualTypeArguments = bound0.getActualTypeArguments();
        assertEquals(Integer.class, actualTypeArguments[0]);
        assertEquals(String.class, actualTypeArguments[1]);

        assertEquals(AutoCloseable.class, bounds[1]);

        var rawClass = typeToken.getRawClass();
        assertEquals(Map.class, rawClass);
    }

    /**
     * A wildcard remains a generic argument of the captured list type.
     */
    @Test
    void testCaptureWildcardType() {
        var typeToken = new TypeToken<List<?>>() {
        };
        var capturedType = assertInstanceOf(ParameterizedType.class, typeToken.getType());
        assertInstanceOf(WildcardType.class, capturedType.getActualTypeArguments()[0]);

        var rawClass = typeToken.getRawClass();
        assertEquals(List.class, rawClass);
    }

    /**
     * An unbounded generic component erases to Object, making the raw array class Object[].
     *
     * @param <T> the type variable
     * @see GenericArrayType
     */
    @Test
    <T> void testCaptureGenericArrayType() {
        var typeToken = new TypeToken<T[]>() {
        };
        var capturedType = assertInstanceOf(GenericArrayType.class, typeToken.getType());
        assertInstanceOf(TypeVariable.class, capturedType.getGenericComponentType());

        var rawClass = typeToken.getRawClass();
        assertEquals(Object[].class, rawClass);
    }

    /**
     * A concrete array is represented directly by its array class.
     */
    @Test
    void testCaptureArrayType() {
        var typeToken = new TypeToken<String[]>() {
        };
        var capturedType = typeToken.getType();
        assertEquals(String[].class, capturedType);

        var rawClass = typeToken.getRawClass();
        assertEquals(String[].class, rawClass);
    }
}
