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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static red.zyc.kit.base.Conditional.when;

/**
 * Unit tests for {@link Conditional}.
 * <p>
 * This test class verifies the behavior of the {@link Conditional} class
 * by running a large number of asynchronous tasks that execute various
 * conditional flows.
 * </p>
 *
 * @author allurx
 */
public class ConditionalTest {

    /**
     * Tests the conditional flow with a large number of asynchronous tasks.
     * <p>
     * This test creates 1,000,000 asynchronous tasks using virtual threads,
     * each executing the {@link #conditionalFlow()} method. The test waits
     * for all tasks to complete before finishing.
     * </p>
     */
    @Test
    void testConditionalFlow() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.allOf(IntStream.range(0, 1000000)
                            .mapToObj(operand -> CompletableFuture.runAsync(this::conditionalFlow, executor))
                            .toArray(CompletableFuture[]::new))
                    .join();
        }
    }

    /**
     * Executes a series of conditional operations and verifies the result.
     * <p>
     * This method demonstrates the usage of {@link Conditional} by defining
     * multiple conditional branches with different outcomes. The expected result
     * is compared with the actual result to ensure correctness.
     * </p>
     */
    void conditionalFlow() {
        var i
                = when(() -> false).run(() -> System.out.println("if"))
                .elseIf(() -> false).run(() -> System.out.println("else if"))
                .elseIf(() -> false).throwIt(RuntimeException::new)
                .elseIf(() -> true).supply(() -> 1)
                .elseIf(() -> false).supply(() -> "2")
                .elseIf(() -> true).supply(() -> 3)
                .elseIf(() -> false).supply(() -> "4")
                .orElse().supply(() -> 5)
                .get();
        Assertions.assertEquals(1, i);
    }

}
