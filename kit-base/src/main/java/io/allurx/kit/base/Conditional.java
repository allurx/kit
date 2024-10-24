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

package io.allurx.kit.base;

import io.allurx.kit.base.function.MultiOutputSupplier;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.allurx.kit.base.reflection.TypeConverter.uncheckedCast;

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
 * @param <T> the type of the input value
 * @param <R> the type of the output value
 * @author allurx
 */
public final class Conditional<T, R> {

    /**
     * The input value for the conditional flow.
     */
    private final T input;

    /**
     * The output value of the conditional flow.
     */
    private R output;

    /**
     * Flag indicating if any condition has been satisfied.
     */
    private boolean hit = false;

    private Conditional(T input) {
        this.input = input;
    }

    /**
     * Creates a new {@code Conditional} instance with the specified input value.
     *
     * @param value the input value for the conditional flow
     * @param <T>   the type of the input value
     * @param <R>   the type of the result
     * @return a new {@code Conditional} instance
     */
    public static <T, R> Conditional<T, R> of(T value) {
        return new Conditional<>(value);
    }

    /**
     * Starts a conditional flow with a given boolean condition.
     *
     * @param condition the initial boolean condition to evaluate
     * @param <T>       the type of the input value
     * @param <R>       the type of the result
     * @return an {@link Conditional.IfBranch} to continue the flow
     */
    public static <T, R> Conditional<T, R>.IfBranch when(boolean condition) {
        return new Conditional<T, R>(null).new IfBranch(condition);
    }

    /**
     * Starts a conditional flow with a {@link BooleanSupplier} that provides the condition.
     *
     * @param booleanSupplier the supplier providing the boolean condition
     * @param <T>             the type of the input value
     * @param <R>             the type of the result
     * @return an {@link Conditional.IfBranch} to continue the flow
     */
    public static <T, R> Conditional<T, R>.IfBranch when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Starts a conditional flow with a predicate to evaluate against the input value.
     *
     * @param predicate the predicate to test the input value
     * @return an {@link IfBranch} to continue the flow
     */
    public IfBranch when(Predicate<? super T> predicate) {
        return new IfBranch(predicate.test(input));
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
    private abstract class DerivableBranch<B extends DerivableBranch<B>> extends BaseBranch<B> {

        DerivableBranch(boolean branchHit) {
            super(branchHit);
        }

        /**
         * Adds an "else if" condition to the flow using a boolean supplier.
         *
         * @param booleanSupplier the supplier providing the condition
         * @return an {@link ElseIfBranch} to continue the flow
         */
        public ElseIfBranch elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch(!hit && booleanSupplier.getAsBoolean());
        }

        /**
         * Adds an "else if" condition based on a predicate.
         *
         * @param predicate the predicate to test
         * @return an {@link ElseIfBranch} to continue the flow
         */
        public ElseIfBranch elseIf(Predicate<? super T> predicate) {
            return new ElseIfBranch(!hit && predicate.test(input));
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
    private abstract class BaseBranch<B extends BaseBranch<B>> implements Branch<T, R> {

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
        public B peek(Consumer<? super R> consumer) {
            if (branchHit) consumer.accept(output);
            return self();
        }

        @Override
        public B set(Supplier<? extends R> supplier) {
            if (branchHit) output = supplier.get();
            return self();
        }

        @Override
        public B map(Function<? super T, ? extends R> function) {
            if (branchHit) output = function.apply(input);
            return self();
        }

        @Override
        public <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return self();
        }

        @Override
        public R get() {
            return output;
        }

        /**
         * Returns the current instance, avoiding self-references
         * that may prevent proper garbage collection and affect performance.
         *
         * @return the current instance
         */
        private B self() {
            return uncheckedCast(this);
        }
    }

    /**
     * Defines operations that can be performed on a branch.
     *
     * @param <T> the type of the input value
     * @param <R> the type of the output value
     */
    private interface Branch<T, R> extends MultiOutputSupplier<R> {

        /**
         * Runs the action if the branch condition is satisfied.
         *
         * @param runnable the action to run
         * @return the current branch instance
         */
        Branch<T, R> run(Runnable runnable);

        /**
         * Processes the output if the condition is satisfied.
         *
         * @param consumer the consumer to handle the output
         * @return the current branch instance
         */
        Branch<T, R> peek(Consumer<? super R> consumer);

        /**
         * Sets the output if the branch condition is satisfied.
         *
         * @param supplier the supplier providing the output
         * @return the current branch instance
         */
        Branch<T, R> set(Supplier<? extends R> supplier);

        /**
         * Maps the input to an output if the branch condition is satisfied.
         *
         * @param function the mapping function
         * @return the current branch instance
         */
        Branch<T, R> map(Function<? super T, ? extends R> function);

        /**
         * Throws an exception if the branch condition is satisfied.
         *
         * @param supplier the exception supplier
         * @param <X>      the type of exception
         * @return the current branch instance
         * @throws X the exception thrown
         */
        <X extends Throwable> Branch<T, R> throwIt(Supplier<? extends X> supplier) throws X;
    }

}

