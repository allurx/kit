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
 *                  when(condition).run(() -> System.out.println("if"))
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
 * <p>
 * Once a condition in {@link Branch} evaluates to true, subsequent conditions and actions are skipped.
 * <p>
 * Note on Thread Safety:
 * <p>
 * This class is not thread-safe. If instances of ConditionalFlow are accessed by multiple threads concurrently,
 * proper synchronization or other concurrency control mechanisms should be applied.In typical single-threaded use cases, thread safety is not a concern.
 * </p>
 *
 * @param <T> the type of the result yielded by the conditions
 * @author allurx
 */
public final class ConditionalFlow<T> {

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


    public static <T> ConditionalFlow<T>.IfBranch when(boolean condition) {
        return new ConditionalFlow<T>().new IfBranch(condition);
    }

    public static <T> ConditionalFlow<T>.IfBranch when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    public class IfBranch extends DerivableBranch<IfBranch> {

        IfBranch(boolean branchHit) {
            super(branchHit);
        }

    }

    public class ElseIfBranch extends DerivableBranch<ElseIfBranch> {

        ElseIfBranch(boolean branchHit) {
            super(branchHit);
        }

    }

    public class ElseBranch extends BaseBranch<ElseBranch> {

        ElseBranch(boolean branchHit) {
            super(branchHit);
        }
    }

    private class DerivableBranch<B> extends BaseBranch<B> {

        DerivableBranch(boolean branchHit) {
            super(branchHit);
        }

        public ElseIfBranch elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch(!hit && booleanSupplier.getAsBoolean());
        }

        public ElseBranch orElse() {
            return new ElseBranch(!hit);
        }
    }

    private class BaseBranch<B> implements Branch<T, B> {

        final boolean branchHit;

        BaseBranch(boolean branchHit) {
            if (branchHit) hit = true;
            this.branchHit = branchHit;
        }

        @Override
        public B run(Runnable runnable) {
            if (branchHit) runnable.run();
            return self();
        }

        @Override
        public B supply(Supplier<? extends T> supplier) {
            if (branchHit) result = supplier.get();
            return self();
        }

        @Override
        public <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return self();
        }

        @Override
        public T get() {
            return result;
        }

        /**
         * 不要保存自身的引用，否则jvm无法及时回收，导致{@link ConditionalFlow}性能急速下降
         *
         * @return 自身
         */
        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }

    }

    private interface Branch<R, B> extends MultiOutputSupplier<R> {

        B run(Runnable runnable);

        B supply(Supplier<? extends R> supplier);

        <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X;
    }
}
