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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * JSON operations using the Jackson library.
 *
 * @author allurx
 */
public class JacksonOperator extends AbstractJsonOperator<JsonMapper> {

    /**
     * Constructor.
     *
     * @param jsonMapper An instance of {@link JsonMapper}
     */
    public JacksonOperator(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public JacksonOperator with(Supplier<JsonMapper> supplier) {
        return new JacksonOperator(supplier.get());
    }

    @Override
    public JacksonOperator with(UnaryOperator<JsonMapper> unaryOperator) {
        return new JacksonOperator(unaryOperator.apply(subject));
    }

    @Override
    public String toJsonString(Object source) {
        try {
            return subject.writeValueAsString(source);
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public String toJsonString(Object source, Type type) {
        try {
            return subject.writerFor(subject.constructType(type)).writeValueAsString(source);
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public Object fromJsonString(String json, Type type) {
        try {
            return subject.readValue(json, subject.constructType(type));
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T fromJsonString(String json, TypeToken<T> typeToken) {
        try {
            return subject.readValue(json, subject.constructType(typeToken.getType()));
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public Object copyProperties(Object source, Type type) {
        try {
            return subject.convertValue(source, subject.constructType(type));
        } catch (JacksonException | IllegalArgumentException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T copyProperties(Object source, TypeToken<T> typeToken) {
        try {
            return subject.convertValue(source, subject.constructType(typeToken.getType()));
        } catch (JacksonException | IllegalArgumentException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public boolean compare(String... jsons) {
        if (jsons.length == 0) {
            throw new IllegalArgumentException("At least one JSON string is required");
        }
        JsonNode first = readTree(jsons[0]);
        return IntStream.range(1, jsons.length).allMatch(i -> first.equals(readTree(jsons[i])));
    }

    /**
     * Parses a JSON string into a JSON tree.
     *
     * @param json The JSON string to parse
     * @return The resulting JSON tree
     */
    private JsonNode readTree(String json) {
        try {
            return subject.readTree(json);
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
