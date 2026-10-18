/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
package io.allurx.kit.base.test.poller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.allurx.kit.base.concurrency.CountBasedPoller;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Checks successful termination before the configured attempt limit.
 *
 * @author allurx
 */
class CountBasedPollerTest {

    CountBasedPoller poller = CountBasedPoller.builder()
            .count(20)
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
}

