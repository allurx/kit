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
import red.zyc.kit.base.concurrency.CallableFunction;
import red.zyc.kit.base.concurrency.Poller;
import red.zyc.kit.base.concurrency.RunnableFunction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static red.zyc.kit.base.concurrency.Poller.throwingRunnable;

/**
 * Tests for the {@link Poller} class to validate its behavior with different functions and scenarios.
 *
 * <p>This includes testing run and call functionality, as well as handling timeouts and predicates.</p>
 *
 * @author allurx
 */
public class PollerTest {

    /**
     * Tests the Poller with a Runnable function that does not throw an exception.
     * <p>This test expects a RuntimeException to be thrown due to the false predicate.</p>
     */
    @Test
    void run() {
        Assertions.assertThrows(RuntimeException.class,
                () -> Poller.<Void, Void>builder()
                        .timing(Duration.ofSeconds(10), Duration.ofMillis(500))
                        .<RunnableFunction<Void>>execute(null, o -> System.out.println(LocalDateTime.now()))
                        .until(o -> false)
                        .onTimeout(throwingRunnable(() -> new RuntimeException("Timeout")))
                        .build()
                        .poll(), "Expected RuntimeException due to the false predicate.");
    }

    /**
     * Tests the Poller with a Callable function that increments an AtomicInteger.
     * <p>This test expects the AtomicInteger to reach the value 12 before timeout.</p>
     */
    @Test
    void call() {
        AtomicInteger num = new AtomicInteger(1);
        Poller.<AtomicInteger, Integer>builder()
                .timing(Duration.ofSeconds(10), Duration.ofMillis(500))
                .<CallableFunction<AtomicInteger, Integer>>execute(num, i -> {
                    System.out.println(num.get());
                    return num.incrementAndGet();
                })
                .until(o -> o == 12)
                .onTimeout(throwingRunnable(() -> new RuntimeException("Timeout")))
                .build()
                .poll();

        Assertions.assertEquals(12, num.get(), "Expected AtomicInteger to reach the value 12.");
    }

    // Optional: Additional tests for edge cases and different scenarios
}
