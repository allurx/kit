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
package red.zyc.kit.base.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import red.zyc.kit.base.poller.Poller;
import red.zyc.kit.base.poller.RunnableFunction;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author allurx
 */
@Fork(1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Threads(value = Threads.MAX)
@Warmup(iterations = 3, time = 8)
@Measurement(iterations = 3, time = 8)
public class PollerJmh {

    Poller<Void, Void> poller = Poller.<Void, Void>builder()
            .timing(Duration.ofSeconds(3), Duration.ofMillis(500))
            .<RunnableFunction<Void>>execute(null, input -> {
            })
            .predicate(o -> false)
            .build();

    @Benchmark
    public void run() {
        poller.poll();
    }

}
