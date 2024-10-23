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
package red.zyc.kit.base.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import red.zyc.kit.base.Conditional;

import static red.zyc.kit.base.Conditional.when;

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
                .elseIf(() -> true).set(() -> 1)
                .elseIf(() -> false).set(() -> "2")
                .elseIf(() -> true).set(() -> 3)
                .elseIf(() -> false).set(() -> "4")
                .orElse().set(() -> 5)
                .get();
        Assertions.assertEquals(1, result);
    }

    /**
     * Tests conditional flow with input and result mapping.
     */
    @Test
    void testConditionalWithInput() {
        var result = Conditional.of(6)
                .when(i -> i <= 3).run(() -> System.out.println("if"))
                .elseIf(i -> i > 3 && i <= 6).run(() -> System.out.println("else if")).map(i -> "result").peek(System.out::println)
                .orElse().run(() -> System.out.println("else"))
                .<String>getAsType();
        Assertions.assertEquals("result", result);
    }

}

