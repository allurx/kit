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
import red.zyc.kit.base.concurrency.IntervalBasedPoller;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author allurx
 */
public class IntervalBasedPollerTest {

    @Test
    void test() {
        var num = new IntervalBasedPoller()
                .timing(Duration.ofSeconds(10), Duration.ofMillis(500))
                .onTimeout(() -> {
                    throw new RuntimeException("timeout");
                })
                .poll(new AtomicInteger(1),
                        i -> {
                            System.out.println(i);
                            return i.incrementAndGet();
                        },
                        o -> o == 12)
                .get();

        Assertions.assertEquals(12, num);
    }

}
