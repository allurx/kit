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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Conditional}.
 *
 * @author allurx
 */
class ConditionalTest {

    /**
     * Tests a conditional flow with multiple branches and returns the result.
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
     * Tests the function of the Conditional class.
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

}

