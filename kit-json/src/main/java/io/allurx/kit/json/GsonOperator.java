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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.allurx.kit.base.reflection.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * JSON operations backed by a retained {@link Gson} instance.
 * Property copying converts through a JSON tree in memory and applies the configured adapters.
 * Gson exceptions propagate without being wrapped in {@link JsonException}.
 *
 * @author allurx
 */
public class GsonOperator extends AbstractJsonOperator<Gson> {

    /**
     * Creates an operator around the supplied Gson instance without copying it.
     *
     * @param gson the non-null Gson backend
     * @throws NullPointerException if the backend is null
     */
    public GsonOperator(Gson gson) {
        super(gson);
    }

    @Override
    public GsonOperator with(Supplier<Gson> supplier) {
        return new GsonOperator(supplier.get());
    }

    @Override
    public GsonOperator with(UnaryOperator<Gson> unaryOperator) {
        return new GsonOperator(unaryOperator.apply(subject));
    }

    @Override
    public String toJsonString(Object source) {
        return subject.toJson(source);
    }

    @Override
    public String toJsonString(Object source, Type type) {
        return subject.toJson(source, type);
    }

    @Override
    public Object fromJsonString(String json, Type type) {
        return subject.fromJson(json, type);
    }

    @Override
    public <T> T fromJsonString(String json, TypeToken<T> typeToken) {
        return subject.fromJson(json, typeToken.getType());
    }

    @Override
    public Object copyProperties(Object source, Type type) {
        return subject.fromJson(subject.toJsonTree(source), type);
    }

    @Override
    public <T> T copyProperties(Object source, TypeToken<T> typeToken) {
        return subject.fromJson(subject.toJsonTree(source), typeToken.getType());
    }

    /**
     * {@inheritDoc}
     * <p>Uses this Gson's parsing settings, including strictness. Numeric values are compared
     * as {@link BigDecimal} throughout nested arrays and objects, so {@code 1}, {@code 1.0},
     * and {@code 1e0} compare equal without losing large-integer or decimal precision.
     * Signed zeros also compare equal. Empty input is rejected; the JSON literal {@code null}
     * is a valid value.
     *
     * @throws NullPointerException if the array or a visited input is null
     * @throws JsonSyntaxException if a visited input is empty, violates the configured syntax rules,
     *                            or contains extra JSON values
     * @throws NumberFormatException if a compared number cannot be represented by {@link BigDecimal}
     */
    @Override
    public boolean compare(String... jsons) {
        return compare(jsons, this::readTree, GsonOperator::equivalent);
    }

    /**
     * Parses a JSON value, rejecting empty input while allowing JSON null.
     *
     * @param json the JSON string to parse
     * @return the parsed JSON value
     */
    private JsonElement readTree(String json) {
        JsonElement value = subject.fromJson(Objects.requireNonNull(json, "json"), JsonElement.class);
        if (value == null) {
            throw new JsonSyntaxException("A JSON value is required");
        }
        return value;
    }

    /**
     * Compares nested values without reducing JSON numbers to {@code double}.
     *
     * @param first the first parsed value
     * @param second the second parsed value
     * @return whether the values are structurally and numerically equivalent
     */
    private static boolean equivalent(JsonElement first, JsonElement second) {
        if (first instanceof JsonPrimitive firstPrimitive && second instanceof JsonPrimitive secondPrimitive
                && firstPrimitive.isNumber() && secondPrimitive.isNumber()) {
            return new BigDecimal(firstPrimitive.getAsString())
                    .compareTo(new BigDecimal(secondPrimitive.getAsString())) == 0;
        }
        if (first instanceof JsonArray firstArray && second instanceof JsonArray secondArray) {
            return firstArray.size() == secondArray.size()
                    && IntStream.range(0, firstArray.size()).allMatch(i -> equivalent(firstArray.get(i), secondArray.get(i)));
        }
        if (first instanceof JsonObject firstObject && second instanceof JsonObject secondObject) {
            return firstObject.keySet().equals(secondObject.keySet())
                    && firstObject.entrySet().stream().allMatch(entry -> equivalent(entry.getValue(), secondObject.get(entry.getKey())));
        }
        return first.equals(second);
    }
}

