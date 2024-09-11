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

package red.zyc.kit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import red.zyc.kit.base.reflection.TypeToken;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Base class for JSON operators, implementing the {@link JsonOperator} interface,
 * providing basic JSON operation functionality.
 *
 * @param <O> The specific type of JSON operator
 * @param <J> The type of the JSON processing entity, such as {@link ObjectMapper} or {@link Gson}
 * @author allurx
 */
public abstract class AbstractJsonOperator<O extends JsonOperator<J>, J> implements JsonOperator<J> {

    /**
     * The current JSON operator instance, typically a self-reference,
     * used for method chaining.
     */
    protected final O jsonOperator;

    /**
     * The entity responsible for performing JSON operations, such as {@link ObjectMapper} or {@link Gson}.
     */
    protected final J subject;

    /**
     * Constructor to initialize the JSON operation entity.
     *
     * @param subject The entity responsible for JSON operations, cannot be null
     */
    @SuppressWarnings("unchecked")
    public AbstractJsonOperator(J subject) {
        if (subject == null) {
            throw new IllegalArgumentException("The JSON operation entity cannot be null");
        }
        this.jsonOperator = (O) this;
        this.subject = subject;
    }

    /**
     * Configures the current JSON operation entity, allowing customization
     * by passing a {@link Consumer}.
     *
     * @param consumer A {@link Consumer} to configure the JSON operation entity
     * @return The current JSON operator instance for method chaining
     */
    @Override
    public O configure(Consumer<J> consumer) {
        consumer.accept(subject);
        return jsonOperator;
    }

    /**
     * Returns the current entity responsible for performing JSON operations.
     *
     * @return The JSON operation entity
     */
    @Override
    public J subject() {
        return subject;
    }

    /**
     * Converts a JSON string to a Java object of the specified {@link TypeToken}.
     *
     * @param json      The JSON string
     * @param typeToken The {@link TypeToken} representing the target type
     * @param <T>       The target type
     * @return The Java object converted from the JSON string
     */
    @Override
    public <T> T fromJsonString(String json, TypeToken<T> typeToken) {
        return fromJsonString(json, typeToken.getType());
    }

    /**
     * Copies the properties from a source object to a target object of the specified type,
     * using JSON serialization and deserialization for conversion.
     *
     * @param source The source object
     * @param type   The target object's {@link Type}
     * @param <T>    The target object type
     * @return The target object
     */
    @Override
    public <T> T copyProperties(Object source, Type type) {
        return fromJsonString(toJsonString(source), type);
    }

    /**
     * Copies the properties from a source object to a target object of the specified {@link TypeToken} type,
     * using JSON serialization and deserialization for conversion.
     *
     * @param source    The source object
     * @param typeToken The {@link TypeToken} representing the target type
     * @param <T>       The target object type
     * @return The target object
     */
    @Override
    public <T> T copyProperties(Object source, TypeToken<T> typeToken) {
        return fromJsonString(toJsonString(source), typeToken.getType());
    }
}
