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
package io.allurx.kit.json.test;

import org.junit.jupiter.api.Test;
import io.allurx.kit.base.reflection.TypeToken;
import tools.jackson.databind.SerializationFeature;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.allurx.kit.json.JsonOperator.GSON_OPERATOR;
import static io.allurx.kit.json.JsonOperator.JACKSON_OPERATOR;

/**
 * Unit tests for JSON conversion, copying, and comparison using
 * both GSON and Jackson JSON operators.
 * <p>
 * These tests verify the correct serialization and deserialization of a list of Person objects,
 * as well as the ability to compare JSON strings and copy object properties.
 * </p>
 *
 * @author allurx
 */
public class JsonTest {

    /**
     * A simple record representing a Person with name, age, phone number, and creation time.
     */
    record Person(String name, int age, String phoneNumber, LocalDateTime createdTime) {
    }

    private static final List<Person> PERSONS =
            Stream.of(new Person("allurx", 18, "12345678900", LocalDateTime.of(2021, 1, 15, 12, 0, 0)),
                            new Person("allurx1", 20, "12345678901", LocalDateTime.of(2021, 1, 15, 12, 0, 0)))
                    .collect(Collectors.toList());

    private static final TypeToken<List<Person>> TYPE_TOKEN = new TypeToken<List<Person>>() {
    };

    /**
     * Tests JSON serialization and deserialization using Jackson.
     */
    @Test
    void testJacksonConversion() {
        String json = JACKSON_OPERATOR.toJsonString(PERSONS);
        assertEquals(PERSONS, JACKSON_OPERATOR.fromJsonString(json, TYPE_TOKEN));
    }

    /**
     * Tests property copying using Jackson.
     */
    @Test
    void testJacksonCopy() {
        List<Person> copy = JACKSON_OPERATOR.copyProperties(PERSONS, TYPE_TOKEN);
        assertEquals(PERSONS, copy);
    }

    /**
     * Compares compact and pretty-printed JSON produced by Jackson.
     */
    @Test
    void testJacksonCompare() {
        var pretty = JACKSON_OPERATOR.with(mapper -> mapper.rebuild()
                .enable(SerializationFeature.INDENT_OUTPUT).build());
        assertTrue(JACKSON_OPERATOR.compare(JACKSON_OPERATOR.toJsonString(PERSONS), pretty.toJsonString(PERSONS)));
    }

    /**
     * Tests JSON serialization and deserialization using GSON.
     */
    @Test
    void testGsonConversion() {
        String json = GSON_OPERATOR.toJsonString(PERSONS);
        assertEquals(PERSONS, GSON_OPERATOR.fromJsonString(json, TYPE_TOKEN));
    }

    /**
     * Tests property copying using GSON.
     */
    @Test
    void testGsonCopy() {
        List<Person> copy = GSON_OPERATOR.copyProperties(PERSONS, TYPE_TOKEN);
        assertEquals(PERSONS, copy);
    }

    /**
     * Compares compact and pretty-printed JSON produced by Gson.
     */
    @Test
    void testGsonCompare() {
        var pretty = GSON_OPERATOR.with(gson -> gson.newBuilder().setPrettyPrinting().create());
        assertTrue(GSON_OPERATOR.compare(GSON_OPERATOR.toJsonString(PERSONS), pretty.toJsonString(PERSONS)));
    }

    /**
     * Distinct numeric values remain unequal, including inside nested objects and arrays.
     * Numeric comparison rejects values outside the supported BigDecimal range.
     */
    @Test
    void testGsonComparePreservesNumericPrecision() {
        assertFalse(GSON_OPERATOR.compare("9007199254740992", "9007199254740993"));
        assertFalse(GSON_OPERATOR.compare(
                "{\"items\":[{\"id\":9007199254740992}]}",
                "{\"items\":[{\"id\":9007199254740993}]}"));
        assertFalse(GSON_OPERATOR.compare("0.1000000000000000001", "0.1000000000000000002"));
        assertFalse(GSON_OPERATOR.compare("1e400", "2e400"));
        assertThrows(NumberFormatException.class, () -> GSON_OPERATOR.compare("1e2147483649", "2e2147483649"));
    }

    /**
     * Equivalent number formats and object field order do not change equality; array order and value types do.
     */
    @Test
    void testGsonCompareRetainsNumericAndStructuralEquality() {
        assertTrue(GSON_OPERATOR.compare("1", "1.0", "1e0"));
        assertTrue(GSON_OPERATOR.compare(
                "{\"values\":[9007199254740993,1.0,true,null,\"x\"],\"zero\":0}",
                "{\"zero\":-0.0,\"values\":[9007199254740993.0,1,true,null,\"x\"]}"));
        assertFalse(GSON_OPERATOR.compare("[1,2]", "[2,1]"));
        assertFalse(GSON_OPERATOR.compare("[1]", "[1,2]"));
        assertFalse(GSON_OPERATOR.compare("{\"a\":null}", "{\"b\":null}"));
        assertFalse(GSON_OPERATOR.compare("1", "\"1\""));
    }
}
