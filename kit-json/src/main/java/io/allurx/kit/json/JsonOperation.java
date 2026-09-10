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

package io.allurx.kit.json;

import io.allurx.kit.base.reflection.TypeToken;

import java.lang.reflect.Type;

/**
 * Serializes and deserializes values using a configured JSON backend.
 * Property discovery, adapters, null handling, and conversion failures follow that backend.
 * Use {@link Class} for a concrete target class or {@link TypeToken} to retain generic arguments;
 * deserialization into a dynamically supplied {@link Type} produces an {@link Object} result.
 *
 * @author allurx
 */
public interface JsonOperation {

    /**
     * Serializes a value using its runtime type and the backend's configuration.
     *
     * @param source the value to serialize, including null
     * @return the JSON representation
     */
    String toJsonString(Object source);

    /**
     * Serializes a value using a declared type, including generic element types.
     * The backend uses this type when resolving serializers and type metadata; runtime subtype
     * handling still follows its configuration.
     *
     * @param source the object to serialize
     * @param type the non-null declared type, compatible with the source value
     * @return the JSON representation
     */
    String toJsonString(Object source, Type type);

    /**
     * Deserializes JSON into a dynamically supplied type.
     * This overload does not infer a statically typed result from the assignment target.
     *
     * @param json the JSON input
     * @param type the non-null target type, including any generic arguments
     * @return the deserialized value, which may be null according to the backend and target type
     */
    Object fromJsonString(String json, Type type);

    /**
     * Converts a JSON string to an object of the specified class.
     *
     * @param json the JSON string
     * @param type the non-null target class; use a type token for parameterized targets
     * @param <T> the target type
     * @return the deserialized value, which may be null according to the backend and target type
     */
    <T> T fromJsonString(String json, Class<T> type);

    /**
     * Deserializes JSON into the type captured by a token, preserving generic arguments.
     * For example, {@code new TypeToken<List<Person>>() {}} retains the list's element type.
     *
     * @param json the JSON input
     * @param typeToken the non-null target type token
     * @param <T> the target type
     * @return the deserialized value, which may be null according to the backend and target type
     */
    <T> T fromJsonString(String json, TypeToken<T> typeToken);
}
