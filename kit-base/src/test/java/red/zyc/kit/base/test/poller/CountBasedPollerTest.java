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
package red.zyc.kit.base.test.poller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import red.zyc.kit.base.concurrency.CountBasedPoller;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author allurx
 */
class CountBasedPollerTest {

    // Create a CountBasedPoller with a maximum of 20 polling attempts
    CountBasedPoller poller = CountBasedPoller.builder()
            .count(20)
            .build();

    /**
     * Test the polling functionality to reach a specific value.
     */
    @Test
    void apply() {
        var ai = new AtomicInteger(0);

        // Poll until the AtomicInteger reaches 6
        var num = poller.poll(() -> ai,
                AtomicInteger::incrementAndGet,
                i -> i == 6).get();

        // Assert that the result is 6
        Assertions.assertEquals(6, num);
    }

    /**
     * Test the polling functionality with a side effect of printing the value.
     */
    @Test
    void run() {
        var ai = new AtomicInteger(0);

        // Poll and print the incremented value until it equals 6
        poller.poll(() -> System.out.println(ai.getAndIncrement()), () -> ai.get() == 6);

        // Assert that the final value is 6
        Assertions.assertEquals(6, ai.get());
    }
}

