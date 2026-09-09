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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.allurx.kit.base.reflection.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * JSON operations using the Gson library.
 *
 * @author allurx
 */
public class GsonOperator extends AbstractJsonOperator<Gson> {

    /**
     * Constructor.
     *
     * @param gson An instance of {@link Gson}
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
     * Compares JSON values structurally, using exact numeric equality.
     * Object field order is ignored, while array order is preserved.
     * Equivalent numeric representations such as {@code 1}, {@code 1.0}, and {@code 1e0} compare equal.
     * Numeric comparison is limited to values supported by {@link BigDecimal#BigDecimal(String)}.
     *
     * @param jsons the JSON strings to compare; must contain at least one element
     * @return whether all JSON strings represent equivalent values
     * @throws NumberFormatException if a number encountered during numeric comparison cannot be represented as a {@code BigDecimal}
     */
    @Override
    public boolean compare(String... jsons) {
        if (jsons.length == 0) {
            throw new IllegalArgumentException("At least one JSON string is required");
        }
        JsonElement first = JsonParser.parseString(jsons[0]);
        return IntStream.range(1, jsons.length).allMatch(i -> equivalent(first, JsonParser.parseString(jsons[i])));
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

