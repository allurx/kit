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
package io.allurx.kit.base.test.poller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.allurx.kit.base.concurrency.IntervalBasedPoller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests polling completion and interruption handling.
 *
 * @author allurx
 */
class IntervalBasedPollerTest {

    IntervalBasedPoller poller = IntervalBasedPoller.builder()
            .timing(Duration.ofSeconds(3), Duration.ofMillis(300))
            .build();

    @Test
    void apply() {
        var ai = new AtomicInteger(0);

        var num = poller.poll(() -> ai,
                AtomicInteger::incrementAndGet,
                i -> i == 6).get();

        Assertions.assertEquals(6, num);
    }

    @Test
    void run() {
        var ai = new AtomicInteger(0);

        poller.poll(() -> System.out.println(ai.getAndIncrement()), () -> ai.get() == 6);

        Assertions.assertEquals(6, ai.get());
    }

    /**
     * An existing interrupt prevents all attempts and remains available to the caller.
     */
    @Test
    void skipsPollingWhenAlreadyInterrupted() {
        var calls = new AtomicInteger();
        var poller = IntervalBasedPoller.builder().build();

        try {
            Thread.currentThread().interrupt();
            var result = poller.poll(calls::incrementAndGet, value -> value, value -> false);

            Assertions.assertEquals(0, calls.get());
            Assertions.assertEquals(0, result.count());
            Assertions.assertNull(result.result());
            Assertions.assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            // Do not leak this test's interrupt into other tests.
            Thread.interrupted();
        }
    }

    /**
     * An interrupt during sleep stops further attempts while preserving the last result and interrupt flag.
     */
    @Test
    void stopsWhenInterruptedDuringSleep() {
        var calls = new AtomicInteger();
        var poller = IntervalBasedPoller.builder()
                .timing(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), Duration.ofSeconds(1), Duration.ofMillis(10))
                .sleeper(ignored -> Thread.currentThread().interrupt())
                .build();

        try {
            // The predicate bounds the test even if interruption is incorrectly ignored.
            var result = poller.poll(calls::incrementAndGet, value -> value, value -> value == 2);

            Assertions.assertEquals(1, calls.get());
            Assertions.assertEquals(1, result.count());
            Assertions.assertEquals(1, result.result());
            Assertions.assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

}

