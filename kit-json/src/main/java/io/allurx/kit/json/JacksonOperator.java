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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * JSON operations using the Jackson library.
 *
 * @author allurx
 */
public class JacksonOperator extends AbstractJsonOperator<JacksonOperator, ObjectMapper> {

    /**
     * Constructor.
     *
     * @param objectMapper An instance of {@link ObjectMapper}
     */
    public JacksonOperator(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public JacksonOperator with(Supplier<ObjectMapper> supplier) {
        return new JacksonOperator(supplier.get());
    }

    @Override
    public JacksonOperator with(UnaryOperator<ObjectMapper> unaryOperator) {
        return new JacksonOperator(unaryOperator.apply(subject));
    }

    @Override
    public String toJsonString(Object source) {
        try {
            return subject.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T fromJsonString(String json, Type type) {
        try {
            return subject.readValue(json, subject.getTypeFactory().constructType(type));
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    @Override
    public boolean compare(String... jsons) {
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
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
