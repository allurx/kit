/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
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
 * JMH throughput comparison of native branching and {@link Conditional}, in operations per millisecond.
 * Both scenarios use constant-false predicates and select the final branch; results describe this
 * specific workload and do not establish performance for arbitrary conditions or callbacks.
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
     * Measures the native branching baseline for the final-branch workload.
     *
     * @param blackhole consumes the result to discourage dead-code elimination
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
     * Measures the fluent equivalent of the native final-branch workload.
     *
     * @param blackhole consumes the result to discourage dead-code elimination
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
