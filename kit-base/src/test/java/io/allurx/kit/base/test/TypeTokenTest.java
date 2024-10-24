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
 * Tests for {@link TypeToken} to capture and validate various type information at runtime.
 * <p>These tests cover capturing generic types, parameterized types, type variables, wildcard types, and array types.</p>
 *
 * @author allurx
 */
class TypeTokenTest {

    /**
     * Tests capturing the class type directly.
     * <p>Expected type is {@code String}.</p>
     *
     * @see Class
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
     * Tests capturing the type when using diamond operator {@code <>}.
     * <p>Expected type is {@code Object} as the compiler infers it.</p>
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
     * Tests capturing a parameterized type, e.g., {@code List<String>}.
     * <p>Checks that the actual type argument is {@code String} and raw class is {@code List}.</p>
     *
     * @see ParameterizedType
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
     * Tests capturing a type variable with bounds, e.g., {@code T extends Map<Integer, String> & AutoCloseable}.
     * <p>Validates bounds and type arguments of the type variable.</p>
     *
     * @param <T> a type variable with bounds
     * @see TypeVariable
     */
    @Test
    <T extends Map<Integer, String> & AutoCloseable> void testCaptureTypeVariable() {
        var typeToken = new TypeToken<T>() {
        };
        var capturedType = assertInstanceOf(TypeVariable.class, typeToken.getType());

        // T's bounds: Map<Integer, String> & AutoCloseable
        var bounds = capturedType.getBounds();

        // Check bounds[0] - Map<Integer, String>
        var bound0 = assertInstanceOf(ParameterizedType.class, bounds[0]);
        var actualTypeArguments = bound0.getActualTypeArguments();
        assertEquals(Integer.class, actualTypeArguments[0]);
        assertEquals(String.class, actualTypeArguments[1]);

        // Check bounds[1] - AutoCloseable
        assertEquals(AutoCloseable.class, bounds[1]);

        // Raw class should be Object
        var rawClass = typeToken.getRawClass();
        assertEquals(Map.class, rawClass);
    }

    /**
     * Tests capturing a wildcard type, e.g., {@code List<?>}.
     * <p>Validates that the wildcard type is correctly captured and the raw class is {@code List}.</p>
     *
     * @see WildcardType
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
     * Tests capturing a generic array type, e.g., {@code T[]}.
     * <p>Validates that the component type is a type variable and raw class is {@code Object[]}.</p>
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
     * Tests capturing a specific array type, e.g., {@code String[]}.
     * <p>Validates that the captured type and raw class are both {@code String[]}.</p>
     *
     * @see Class#isArray()
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
