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
package io.allurx.kit.base.benchmark;

import io.allurx.kit.base.Conditional;
import io.allurx.kit.base.constant.FunctionConstants;
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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark tests comparing native if-else constructs with the Conditional class implementation.
 * This class uses the JMH framework to measure throughput in operations per millisecond.
 * The test scenarios involve:
 * <ui>
 * <li>A standard if-else construct.</li>
 * <li>The Conditional implementation to handle the same logic.</li>
 * </ui>
 * Each test will run with several iterations of warmup and measurement to gather performance data.
 *
 * @author allurx
 */
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Threads(Threads.MAX)
@Warmup(iterations = 3, time = 8)
@Measurement(iterations = 3, time = 8)
public class ConditionalBenchmark {

    /**
     * Native if-else construct for testing basic conditional logic performance.
     * This method simulates a simple conditional flow with multiple branches using standard Java syntax.
     *
     * @param blackhole Blackhole to consume the result and prevent JIT optimizations
     */
    @Benchmark
    public void testNativeConditional(Blackhole blackhole) {
        String result;
        if (FunctionConstants.FALSE_PREDICATE.test(null)) {
            result = "if";
        } else if (FunctionConstants.FALSE_PREDICATE.test(null)) {
            result = "else if";
        } else {
            result = "else";
        }
        blackhole.consume(result);
    }

    /**
     * Test using  {@link Conditional} to handle the same conditional logic.
     * This method demonstrates how the Conditional API is used to replace standard if-else logic.
     *
     * @param blackhole Blackhole to consume the result and prevent JIT optimizations
     */
    @Benchmark
    public void testConditional(Blackhole blackhole) {
        var result
                = Conditional.of(FunctionConstants.FALSE_PREDICATE)
                .when(predicate -> predicate.test(null)).map(o -> "if")
                .elseIf(predicate -> predicate.test(null)).map(o -> "else if")
                .orElse().map(o -> "else")
                .get();
        blackhole.consume(result);
    }
}
