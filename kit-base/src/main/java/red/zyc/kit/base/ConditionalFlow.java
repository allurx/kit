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
 * A fluent API for conditional branching, similar to if-else statements.
 * This allows constructing complex conditional logic in a readable and concise way.
 * <p>
 * Example usage:
 * <pre>{@code
 *      when(condition).run(() -> System.out.println("if"))
 *     .elseIf(condition).run(() -> System.out.println("else if"))
 *     .orElse().run(() -> System.out.println("else"));
 * }</pre>
 * Equivalent to:
 * <pre>{@code
 *     if (condition) {
 *         System.out.println("if");
 *     } else if (condition) {
 *         System.out.println("else if");
 *     } else {
 *         System.out.println("else");
 *     }
 * }</pre>
 * <p>
 * Supports short-circuit evaluation: Once a condition evaluates to {@code true},
 * further conditions and actions are skipped.
 * <p>
 * <strong>Note:</strong> This class is not thread-safe. Proper synchronization is needed
 * for concurrent use.
 *
 * @param <T> the type of the result produced by the conditions
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
     * Flag indicating if any condition has been satisfied.
     */
    private boolean hit = false;

    /**
     * Starts a conditional flow with the given condition.
     *
     * @param condition the initial condition
     * @param <T>       the type of result expected
     * @return an {@link IfBranch} to continue the flow
     */
    public static <T> ConditionalFlow<T>.IfBranch when(boolean condition) {
        return new ConditionalFlow<T>().new IfBranch(condition);
    }

    /**
     * Starts a conditional flow using a {@link BooleanSupplier} for the condition.
     *
     * @param booleanSupplier the supplier providing the condition
     * @param <T>             the type of result expected
     * @return an {@link IfBranch} to proceed with the flow
     */
    public static <T> ConditionalFlow<T>.IfBranch when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Represents the "if" branch in the conditional flow.
     */
    public class IfBranch extends DerivableBranch<IfBranch> {
        IfBranch(boolean branchHit) {
            super(branchHit);
        }
    }

    /**
     * Represents the "else if" branch in the conditional flow.
     */
    public class ElseIfBranch extends DerivableBranch<ElseIfBranch> {
        ElseIfBranch(boolean branchHit) {
            super(branchHit);
        }
    }

    /**
     * Represents the "else" branch in the conditional flow.
     */
    public class ElseBranch extends BaseBranch<ElseBranch> {
        ElseBranch(boolean branchHit) {
            super(branchHit);
        }
    }

    /**
     * Base class for branches that can derive other branches like else-if or else.
     *
     * @param <B> the type of the derived branch
     */
    private class DerivableBranch<B> extends BaseBranch<B> {

        DerivableBranch(boolean branchHit) {
            super(branchHit);
        }

        /**
         * Adds an "else if" condition to the flow.
         *
         * @param booleanSupplier the supplier providing the condition
         * @return an {@link ElseIfBranch} to continue the flow
         */
        public ElseIfBranch elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch(!hit && booleanSupplier.getAsBoolean());
        }

        /**
         * Adds an "else" block to the flow.
         *
         * @return an {@link ElseBranch} to finalize the flow
         */
        public ElseBranch orElse() {
            return new ElseBranch(!hit);
        }
    }

    /**
     * Base class for all branches (if, else-if, else) in the conditional flow.
     *
     * @param <B> the type of the current branch
     */
    private class BaseBranch<B> implements Branch<T, B> {

        /**
         * Whether the current branch condition is satisfied.
         */
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

        /**
         * Returns the result produced by the branch where the condition was met,
         * or {@code null} if no conditions were satisfied.
         *
         * @return the result from the matched branch, or {@code null}
         */
        @Override
        public T get() {
            return result;
        }

        /**
         * Returns the current instance, avoiding self-references
         * that may prevent proper garbage collection and affect performance.
         *
         * @return the current instance
         */
        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }
    }

    /**
     * Defines operations that can be performed on a branch.
     *
     * @param <R> the result type returned by the branch
     * @param <B> the type of the current branch
     */
    private interface Branch<R, B> extends MultiOutputSupplier<R> {

        /**
         * Executes the provided {@link Runnable} if the branch condition is satisfied.
         *
         * @param runnable the action to execute
         * @return the current branch instance
         */
        B run(Runnable runnable);

        /**
         * Supplies a result if the branch condition is satisfied.
         *
         * @param supplier the supplier providing the result
         * @return the current branch instance
         */
        B supply(Supplier<? extends R> supplier);

        /**
         * Throws an exception if the branch condition is satisfied.
         *
         * @param supplier the supplier providing the exception
         * @param <X>      the type of exception
         * @return the current branch instance
         * @throws X the exception thrown
         */
        <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X;
    }
}