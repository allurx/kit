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
 * JSON operations backed by a retained Jackson {@link JsonMapper}.
 * Property copying uses {@code convertValue} in memory rather than a JSON string round trip.
 * Jackson failures are wrapped in {@link JsonException}; {@link IllegalArgumentException} from
 * property conversion is wrapped as well. Other argument errors retain the backend's exception type.
 *
 * @author allurx
 */
public class JacksonOperator extends AbstractJsonOperator<JsonMapper> {

    /**
     * Reads exact, normalized decimals for comparison without changing the backend.
     */
    private final ObjectReader comparisonReader;

    /**
     * Creates an operator around the supplied mapper without copying it.
     * A separate reader enables precise decimal comparison without changing mapper settings
     * used by serialization, deserialization, or property conversion.
     *
     * @param jsonMapper the non-null Jackson backend
     * @throws NullPointerException if the backend is null
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
     * For example, {@code 1.0} equals {@code 1e0}, but integer {@code 1} remains distinct from both.
     * All other equality rules use {@link JsonNode#equals(Object)}.
     * Other parsing settings, including trailing-token handling, follow this mapper.
     * Empty or whitespace-only input is represented by Jackson's missing node and compares equal
     * to other empty inputs, but not to the JSON literal {@code null}.
     *
     * @throws JsonException if Jackson reports a parsing error
     * @throws NumberFormatException if a decimal exceeds {@code BigDecimal}'s range
     * @throws IllegalArgumentException if a visited input is null or no inputs are supplied
     */
    @Override
    public boolean compare(String... jsons) {
        return compare(jsons, this::readTree, JsonNode::equals);
    }

    /**
     * Parses a tree using the comparison reader.
     *
     * @param json the JSON string to parse
     * @return the resulting tree, including a missing node for empty input
     */
    private JsonNode readTree(String json) {
        try {
            return comparisonReader.readTree(json);
        } catch (JacksonException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }
}
