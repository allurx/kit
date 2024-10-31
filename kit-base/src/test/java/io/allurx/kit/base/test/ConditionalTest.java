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

import static io.allurx.kit.base.Conditional.when;

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
        var result
                = when(() -> false).run(() -> System.out.println("if"))
                .elseIf(() -> false).run(() -> System.out.println("else if"))
                .elseIf(() -> false).throwIt(RuntimeException::new)
                .elseIf(() -> true).map(o -> 1)
                .elseIf(() -> false).map(o -> "2")
                .elseIf(() -> true).map(o -> 3)
                .elseIf(() -> false).map(o -> "4")
                .elseIf(() -> false).map(o -> 5)
                .orElse().map(o -> "6")
                .<Integer>getAsType();
        Assertions.assertEquals(1, result);
    }

    /**
     * Tests conditional flow with input and result mapping.
     */
    @Test
    void testConditionalWithInput() {
        var result = Conditional.of(6)
                .when(i -> i <= 3).run(() -> System.out.println("if"))
                .elseIf(i -> i > 3 && i <= 6).run(() -> System.out.println("else if")).map(i -> "else if")
                .orElse().run(() -> System.out.println("else"))
                .<String>getAsType();
        Assertions.assertEquals("else if", result);
    }

}

