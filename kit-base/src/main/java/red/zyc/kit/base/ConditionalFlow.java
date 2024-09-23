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
package red.zyc.kit.base;

import red.zyc.kit.base.function.MultiOutputSupplier;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * <p>
 * Provides a fluent API for conditional branching, similar to if-else statements.
 * It allows for constructing complex conditional logic in a readable and concise manner.
 * </p>
 * Example usage:
 * <pre>
 *     {@code
 *         ConditionalFlow.when(condition)
 *                 .run(() -> System.out.println("if"))
 *                 .elseIf(condition).run(() -> System.out.println("else if"))
 *                 .orElse().run(() -> System.out.println("else"));
 *     }
 * </pre>
 * This is equivalent to:
 * <pre>
 * {@code
 *     if (condition) {
 *         System.out.println("if");
 *     } else if (condition) {
 *         System.out.println("else if");
 *     } else {
 *         System.out.println("else");
 *     }
 * }
 * </pre>
 * The class supports short-circuit evaluation:
 * <ul>
 *     <li>Once a condition in {@link #when} or {@link #elseIf} evaluates to true, subsequent conditions and actions are skipped.</li>
 *     <li>{@link #when} is evaluated only once, allowing for performance optimizations.</li>
 *     <li>{@link #elseIf} uses a {@link BooleanSupplier} to defer condition evaluation, managing performance and security concerns.</li>
 * </ul>
 * Note on Thread Safety:
 * <p>
 * This class is not thread-safe. If instances of ConditionalFlow are accessed by multiple threads concurrently,
 * proper synchronization or other concurrency control mechanisms should be applied.In typical single-threaded use cases, thread safety is not a concern.
 * </p>
 *
 * @param <T> the type of the result yielded by the conditions
 * @author allurx
 */
public final class ConditionalFlow<T> implements MultiOutputSupplier<T> {

    private ConditionalFlow() {
    }

    /**
     * The result of the conditional flow.
     */
    private T result;

    /**
     * Indicates whether any condition has been met.
     */
    private boolean hit = false;

    /**
     * Starts a new conditional flow with the given condition.
     *
     * @param condition the condition to evaluate
     * @param <T>       the type of result
     * @return a new {@link ConditionalFlow.IfBranch} instance
     */
    public static <T> ConditionalFlow<T>.IfBranch when(boolean condition) {
        return new ConditionalFlow<T>().new IfBranch(condition);
    }

    /**
     * Starts a new conditional flow with a condition provided by a {@link BooleanSupplier}.
     *
     * @param booleanSupplier a supplier providing the condition
     * @param <T>             the type of result
     * @return a new {@link ConditionalFlow.IfBranch} instance
     */
    public static <T> ConditionalFlow<T>.IfBranch when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Creates an {@link ElseIfBranch} if the previous conditions were not met and the provided condition evaluates to true.
     *
     * @param booleanSupplier a supplier providing the condition
     * @return a new {@link ElseIfBranch} instance
     */
    public ElseIfBranch elseIf(BooleanSupplier booleanSupplier) {
        return new ElseIfBranch(!hit && booleanSupplier.getAsBoolean());
    }

    /**
     * Creates an {@link ElseBranch} if none of the previous conditions were met.
     *
     * @return a new {@link ElseBranch} instance
     */
    public ElseBranch orElse() {
        return new ElseBranch(!hit);
    }

    @Override
    public T get() {
        return result;
    }

    /**
     * if branch
     */
    public class IfBranch extends AbstractBranch<ConditionalFlow<T>> {

        IfBranch(boolean branchHit) {
            super(branchHit);
        }

        @Override
        ConditionalFlow<T> context() {
            return ConditionalFlow.this;
        }
    }

    /**
     * else if branch
     */
    public class ElseIfBranch extends AbstractBranch<ConditionalFlow<T>> {

        ElseIfBranch(boolean branchHit) {
            super(branchHit);
        }

        @Override
        ConditionalFlow<T> context() {
            return ConditionalFlow.this;
        }
    }

    /**
     * else branch
     */
    public class ElseBranch extends AbstractBranch<ElseBranch> implements MultiOutputSupplier<T> {

        ElseBranch(boolean branchHit) {
            super(branchHit);
        }

        @Override
        ElseBranch context() {
            return this;
        }

        @Override
        public T get() {
            return ConditionalFlow.this.get();
        }
    }

    /**
     * An abstract base class for defining branches in the conditional flow.
     *
     * @param <C> the type of context returned by branch methods
     */
    private abstract class AbstractBranch<C> implements Branch<T, C> {

        final boolean branchHit;

        AbstractBranch(boolean branchHit) {
            if (branchHit) ConditionalFlow.this.hit = true;
            this.branchHit = branchHit;
        }

        @Override
        public C run(Runnable runnable) {
            if (branchHit) runnable.run();
            return context();
        }

        @Override
        public C supply(Supplier<? extends T> supplier) {
            if (branchHit) ConditionalFlow.this.result = supplier.get();
            return context();
        }

        @Override
        public <X extends Throwable> C throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return context();
        }

        abstract C context();
    }

    /**
     * Defines branch operations in the conditional flow.
     *
     * @param <R> the type of result yielded by the branch
     * @param <C> the type of context returned by branch methods
     */
    private interface Branch<R, C> {

        C run(Runnable runnable);

        C supply(Supplier<? extends R> supplier);

        <X extends Throwable> C throwIt(Supplier<? extends X> supplier) throws X;
    }
}
