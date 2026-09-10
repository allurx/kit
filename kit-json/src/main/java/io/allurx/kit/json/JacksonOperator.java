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
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * JSON operations using the Jackson library.
 *
 * @author allurx
 */
public class JacksonOperator extends AbstractJsonOperator<JsonMapper> {

    /**
     * Reads exact, normalized decimals for comparison without changing the backend.
     */
    private final ObjectReader comparisonReader;

    /**
     * Constructor.
     *
     * @param jsonMapper An instance of {@link JsonMapper}
     */
    public JacksonOperator(JsonMapper jsonMapper) {
        super(jsonMapper);
        comparisonReader = jsonMapper.reader().withFeatures(
                JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);
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

    /**
     * {@inheritDoc}
     * <p>Compares exact decimal values after stripping trailing zeros; signed decimal zeros compare equal.
     * Integer and decimal nodes remain distinct. Other parsing settings follow this mapper.
     *
     * @throws JsonException if Jackson reports a parsing error
     * @throws NumberFormatException if a decimal exceeds {@code BigDecimal}'s range
     */
    @Override
    public boolean compare(String... jsons) {
        return compare(jsons, this::readTree, JsonNode::equals);
    }

    /**
     * Parses a tree using the comparison reader.
     *
     * @param json The JSON string to parse
     * @return The resulting JSON tree
     */
    private JsonNode readTree(String json) {
        try {
            return comparisonReader.readTree(json);
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
