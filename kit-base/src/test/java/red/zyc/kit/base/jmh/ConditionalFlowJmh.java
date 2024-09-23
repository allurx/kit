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
import org.openjdk.jmh.infra.Blackhole;
import red.zyc.kit.base.ConditionalFlow;
import red.zyc.kit.base.constant.FunctionConstants;

import java.util.concurrent.TimeUnit;

import static red.zyc.kit.base.ConditionalFlow.when;

/**
 * Benchmark tests comparing native if-else constructs with the ConditionalFlow class implementation.
 * This class uses the JMH framework to measure throughput in operations per millisecond.
 * The test scenarios involve:
 * <ui>
 * <li>A standard if-else construct.</li>
 * <li>The ConditionalFlow implementation to handle the same logic.</li>
 * </ui>
 * Each test will run with several iterations of warmup and measurement to gather performance data.
 *
 * @author allurx
 */
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Threads(Threads.MAX)
@Warmup(iterations = 3, time = 8)
@Measurement(iterations = 3, time = 8)
public class ConditionalFlowJmh {

    /**
     * Native if-else construct for testing basic conditional logic performance.
     * This method simulates a simple conditional flow with multiple branches using standard Java syntax.
     *
     * @param blackhole Blackhole to consume the result and prevent JIT optimizations
     */
    @Benchmark
    public void testNativeConditionalFlow(Blackhole blackhole) {
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
     * Test using  {@link ConditionalFlow} to handle the same conditional logic.
     * This method demonstrates how the ConditionalFlow API is used to replace standard if-else logic.
     *
     * @param blackhole Blackhole to consume the result and prevent JIT optimizations
     */
    @Benchmark
    public void testConditionalFlow(Blackhole blackhole) {
        var result
                = when(FunctionConstants.FALSE_PREDICATE.test(null)).supply(() -> "if")
                .elseIf(() -> FunctionConstants.FALSE_PREDICATE.test(null)).supply(() -> "else if")
                .orElse().supply(() -> "else")
                .get();
        blackhole.consume(result);
    }
}
