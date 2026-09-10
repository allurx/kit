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
package io.allurx.kit.base.test;

import io.allurx.kit.base.Conditional;
import io.allurx.kit.base.function.MultiOutputSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Unit tests for {@link Conditional}.
 *
 * @author allurx
 */
class ConditionalTest {

    /**
     * The first match supplies the result even when later branches declare different mapping types.
     */
    @Test
    void testConditional() {
        var result = Conditional.of("1")
                .when(() -> false).map(s -> 2)
                .elseIf(() -> false).map(s -> "3")
                .elseIf(() -> false).map(o -> 4)
                .elseIf(() -> false).map(o -> "5")
                .elseIf(() -> true).map(o -> 6)
                .elseIf(() -> true).map(o -> "7")
                .elseIf(() -> false).map(o -> 8)
                .orElse().map(o -> "9")
                .<Integer>getAsType();
        Assertions.assertEquals(6, result);
    }

    /**
     * A matching branch passes its mapped value to following callbacks and result access.
     */
    @Test
    void testConditionalFunction() {
        var result = Conditional.of(6)
                .when(i -> i == 6)
                .map(i -> i * 2)
                .consume(i -> System.out.printf("result: %s%n", i))
                .get();
        Assertions.assertEquals(12, result);
    }

    @Test
    void unmatchedMappingExposesOriginalInputAsObject() {
        MultiOutputSupplier<Object> result = Conditional.of("abc")
                .when(false).map(String::length);

        Assertions.assertEquals("abc", result.get());
        Assertions.assertEquals(Optional.of("abc"), result.getAsOptional());
        Assertions.assertEquals(List.of("abc"), result.getAsStream().toList());
        Assertions.assertEquals("abc", result.getAsConditional().when(false).get());
    }

    @Test
    void skippedBranchesPreserveTheSelectedValue() {
        MultiOutputSupplier<Object> result = Conditional.of("abc")
                .when(true).map(String::length)
                .elseIf(() -> Assertions.fail("The later condition must be skipped"))
                .map(value -> Assertions.fail("The later mapping must be skipped"))
                .orElse();

        Assertions.assertEquals(3, result.get());
        Assertions.assertNull(Conditional.of("abc").when(true).map(value -> (String) null)
                .orElse().map(value -> Assertions.fail("A selected null must not activate fallback")).get());
    }

    @Test
    void mappingPreservesEarlierBranchValuesAndCallbackTypes() {
        var original = Conditional.of("abc").when(true);
        var mapped = original.map(String::length);

        original.consume(value -> Assertions.assertEquals("ABC", value.toUpperCase()));
        Assertions.assertEquals("abc", original.get());
        Assertions.assertEquals(3, mapped.get());
    }

}

