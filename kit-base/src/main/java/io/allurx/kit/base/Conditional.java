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
 * Provides a fluent API for constructing conditional branching logic,
 * similar to traditional if-elseif-else statements. This class enables
 * building complex conditional structures in a concise and readable format.
 *
 * <p>Usage example:
 * <pre>{@code
 *      Conditional.of(input)
 *     .when(condition).run(() -> System.out.println("if"))
 *     .elseIf(condition).run(() -> System.out.println("else if"))
 *     .orElse().run(() -> System.out.println("else"));
 * }</pre>
 *
 * <p>This is equivalent to:
 * <pre>{@code
 *     if (condition) {
 *         System.out.println("if");
 *     } else if (condition) {
 *         System.out.println("else if");
 *     } else {
 *         System.out.println("else");
 *     }
 * }</pre>
 *
 * <p>Supports short-circuiting: once a condition is satisfied, subsequent conditions
 * and actions are skipped.
 *
 * <p><strong>Note:</strong> This class is not thread-safe. Ensure proper synchronization
 * if using in a concurrent environment.
 *
 * @param <I> the type of the input value
 * @author allurx
 */
public final class Conditional<I> {

    /**
     * The input used in conditional flow.
     */
    private final I input;

    /**
     * Indicates if any condition has been met.
     */
    private boolean hit = false;

    private Conditional(I input) {
        this.input = input;
    }

    /**
     * Creates a new {@code Conditional} instance with the specified input.
     *
     * @param input the input for the conditional flow
     * @param <I>   the type of input value
     * @return a new {@code Conditional} instance
     */
    public static <I> Conditional<I> of(I input) {
        return new Conditional<>(input);
    }

    /**
     * Initiates a conditional flow with a specified boolean condition.
     *
     * @param condition the initial condition to evaluate
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<I> when(boolean condition) {
        return new IfBranch<>(condition, input);
    }

    /**
     * Initiates a conditional flow using a {@link BooleanSupplier} to evaluate the condition.
     *
     * @param booleanSupplier the supplier that provides the boolean condition
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<I> when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Initiates a conditional flow with a predicate that tests the input.
     *
     * @param predicate the predicate used to evaluate the input
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<I> when(Predicate<? super I> predicate) {
        return when(predicate.test(input));
    }

    /**
     * Represents the "if" branch of the conditional flow.
     *
     * @param <O> the type of the output produced by this branch
     */
    public class IfBranch<O> extends DerivableBranch<O, IfBranch<O>> {

        IfBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        @Override
        public <U> IfBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new IfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

    }

    /**
     * Represents an "else if" branch in the conditional flow.
     *
     * @param <O> the type of the output produced by this branch
     */
    public class ElseIfBranch<O> extends DerivableBranch<O, ElseIfBranch<O>> {

        ElseIfBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        @Override
        public <U> ElseIfBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new ElseIfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

    }

    /**
     * Represents the "else" branch in the conditional flow.
     *
     * @param <O> the type of the output produced by this branch
     */
    public class ElseBranch<O> extends BaseBranch<O, ElseBranch<O>> {

        ElseBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        @Override
        public <U> ElseBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new ElseBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }
    }

    /**
     * Base class for branches that can derive "else if" or "else" branches.
     *
     * @param <O> the type of the output produced by this branch
     * @param <B> the type of derived branch
     */
    private abstract class DerivableBranch<O, B extends DerivableBranch<O, B>> extends BaseBranch<O, B> {

        DerivableBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        /**
         * Adds an "else if" branch using a boolean supplier.
         *
         * @param booleanSupplier the supplier providing the condition
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         */
        public ElseIfBranch<I> elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch<>(!hit && booleanSupplier.getAsBoolean(), getAsType());
        }

        /**
         * Adds an "else if" branch using a predicate to evaluate the input.
         *
         * @param predicate the predicate that tests the input
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         */
        public ElseIfBranch<I> elseIf(Predicate<? super I> predicate) {
            return new ElseIfBranch<>(!hit && predicate.test(input), getAsType());
        }

        /**
         * Adds an "else" branch to complete the conditional flow.
         *
         * @return an {@link ElseBranch} instance to finish the flow
         */
        public ElseBranch<I> orElse() {
            return new ElseBranch<>(!hit, getAsType());
        }
    }

    /**
     * Base class for all conditional branches (if, else-if, else).
     *
     * @param <O> the type of the output produced by this branch
     * @param <B> the type of this branch
     */
    private abstract class BaseBranch<O, B extends BaseBranch<O, B>> implements Branch<O> {

        /**
         * The output produced by this branch.
         */
        final O output;

        /**
         * Indicates if the current branch condition was met.
         */
        final boolean branchHit;

        BaseBranch(boolean branchHit, O output) {
            if (branchHit) hit = true;
            this.branchHit = branchHit;
            this.output = output;
        }

        @Override
        public B run(Runnable runnable) {
            if (branchHit) runnable.run();
            return self();
        }

        @Override
        public B consume(Consumer<? super O> consumer) {
            if (branchHit) consumer.accept(output);
            return self();
        }

        @Override
        public <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return self();
        }

        /**
         * Returns the output of the current branch.
         * <p>
         * Use {@link #getAsType()} to specify the desired type when needed.
         *
         * @return the result of the current branch
         */
        @Override
        public O get() {
            return output;
        }

        /**
         * Returns the current branch instance itself, optimizing for type inference and performance.
         *
         * @return the current instance with inferred type
         */
        B self() {
            return uncheckedCast(this);
        }
    }

    /**
     * Defines operations that can be performed on a branch.
     *
     * @param <O> the type of the output
     */
    private interface Branch<O> extends MultiOutputSupplier<O> {

        /**
         * Executes the specified action if the condition is met.
         *
         * @param runnable the action to execute
         * @return this branch for chaining
         */
        Branch<O> run(Runnable runnable);

        /**
         * Processes the output if the condition is met.
         *
         * @param consumer the output processor
         * @return this branch for chaining
         */
        Branch<O> consume(Consumer<? super O> consumer);

        /**
         * Maps the output to a new value if the condition is met.
         *
         * @param function the mapping function
         * @param <U>      the new output type
         * @return a new branch with the mapped output
         */
        <U> Branch<U> map(Function<? super O, ? extends U> function);

        /**
         * Throws an exception if the condition is met.
         *
         * @param supplier the supplier for the exception
         * @param <X>      the exception type
         * @return this branch for chaining
         * @throws X the exception thrown if the condition is met
         */
        <X extends Throwable> Branch<O> throwIt(Supplier<? extends X> supplier) throws X;
    }

}



