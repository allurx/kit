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
 * Defines basic JSON operations.
 *
 * @author allurx
 */
public interface JsonOperation {

    /**
     * Converts a Java object to its JSON string representation.
     *
     * @param source The Java object to be converted.
     * @return The JSON string representation of the Java object.
     */
    String toJsonString(Object source);

    /**
     * Converts a JSON string to a Java object of the specified {@link Type}.
     *
     * @param json The JSON string.
     * @param type The {@link Type} of the Java object to be created.
     * @param <T>  The type of the Java object to be created.
     * @return The Java object represented by the JSON string.
     */
    <T> T fromJsonString(String json, Type type);

    /**
     * Converts a JSON string to a Java object based on the specified {@link TypeToken#getType()}.
     *
     * @param json      The JSON string.
     * @param typeToken The {@link TypeToken} representing the type of the Java object to be created.
     * @param <T>       The type of the Java object to be created.
     * @return The Java object represented by the JSON string.
     */
    <T> T fromJsonString(String json, TypeToken<T> typeToken);
}
