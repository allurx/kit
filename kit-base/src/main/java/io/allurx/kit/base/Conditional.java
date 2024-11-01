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

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.allurx.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * Provides a fluent API for constructing conditional branching logic,
 * similar to traditional if-elseif-else statements. This class allows
 * for building complex conditional structures in a concise and readable format.
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
 * @param <T> the type of the input value
 * @author allurx
 */
public final class Conditional<T> {

    /**
     * The input value used in conditional flow.
     */
    private final T input;

    /**
     * Indicates if any condition has been met.
     */
    private boolean hit = false;

    private Conditional(T input) {
        this.input = input;
    }

    /**
     * Creates a new {@code Conditional} instance with the specified input.
     *
     * @param input the input value for the conditional flow
     * @param <T>   the type of input value
     * @return a new {@code Conditional} instance
     */
    public static <T> Conditional<T> of(T input) {
        return new Conditional<>(input);
    }

    /**
     * Initiates a conditional flow with a specified boolean condition.
     *
     * @param condition the initial condition to evaluate
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<T> when(boolean condition) {
        return new IfBranch<>(condition, input);
    }

    /**
     * Initiates a conditional flow using a {@link BooleanSupplier} to evaluate the condition.
     *
     * @param booleanSupplier the supplier that provides the boolean condition
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<T> when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Initiates a conditional flow with a predicate that tests the input value.
     *
     * @param predicate the predicate used to evaluate the input value
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<T> when(Predicate<? super T> predicate) {
        return when(predicate.test(input));
    }

    /**
     * Represents the "if" branch of the conditional flow.
     *
     * @param <R> the type of the output value produced by this branch
     */
    public class IfBranch<R> extends DerivableBranch<R, IfBranch<R>> {

        IfBranch(boolean branchHit, R output) {
            super(branchHit, output);
        }

        @Override
        public <O> IfBranch<O> map(Function<? super R, ? extends O> function) {
            return branchHit ? new IfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

        @Override
        public <O> IfBranch<O> map(BiFunction<? super T, ? super R, ? extends O> function) {
            return branchHit ? new IfBranch<>(true, function.apply(input, output)) : uncheckedCast(this);
        }

    }

    /**
     * Represents an "else if" branch in the conditional flow.
     *
     * @param <R> the type of the output value produced by this branch
     */
    public class ElseIfBranch<R> extends DerivableBranch<R, ElseIfBranch<R>> {

        ElseIfBranch(boolean branchHit, R output) {
            super(branchHit, output);
        }

        @Override
        public <O> ElseIfBranch<O> map(Function<? super R, ? extends O> function) {
            return branchHit ? new ElseIfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

        @Override
        public <O> ElseIfBranch<O> map(BiFunction<? super T, ? super R, ? extends O> function) {
            return branchHit ? new ElseIfBranch<>(true, function.apply(input, output)) : uncheckedCast(this);
        }
    }

    /**
     * Represents the "else" branch in the conditional flow.
     *
     * @param <R> the type of the output value produced by this branch
     */
    public class ElseBranch<R> extends BaseBranch<R, ElseBranch<R>> {

        ElseBranch(boolean branchHit, R output) {
            super(branchHit, output);
        }

        @Override
        public <O> ElseBranch<O> map(Function<? super R, ? extends O> function) {
            return branchHit ? new ElseBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

        @Override
        public <O> ElseBranch<O> map(BiFunction<? super T, ? super R, ? extends O> function) {
            return branchHit ? new ElseBranch<>(true, function.apply(input, output)) : uncheckedCast(this);
        }
    }

    /**
     * Base class for branches that can derive "else if" or "else" branches.
     *
     * @param <R> the type of the output value produced by this branch
     * @param <B> the type of derived branch
     */
    private abstract class DerivableBranch<R, B extends DerivableBranch<R, B>> extends BaseBranch<R, B> {

        DerivableBranch(boolean branchHit, R output) {
            super(branchHit, output);
        }

        /**
         * Adds an "else if" branch using a boolean supplier.
         *
         * @param booleanSupplier the supplier providing the condition
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         */
        public ElseIfBranch<T> elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch<>(!hit && booleanSupplier.getAsBoolean(), getAsType());
        }

        /**
         * Adds an "else if" branch using a predicate to evaluate the input value.
         *
         * @param predicate the predicate that tests the input value; if true, the corresponding branch is executed
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         */
        public ElseIfBranch<T> elseIf(Predicate<? super T> predicate) {
            return new ElseIfBranch<>(!hit && predicate.test(input), getAsType());
        }

        /**
         * Adds an "else" branch to complete the conditional flow.
         *
         * @return an {@link ElseBranch} instance to finish the flow
         */
        public ElseBranch<T> orElse() {
            return new ElseBranch<>(!hit, getAsType());
        }
    }

    /**
     * Base class for all conditional branches (if, else-if, else).
     *
     * @param <R> the type of the output value produced by this branch
     * @param <B> the type of the current branch
     */
    private abstract class BaseBranch<R, B extends BaseBranch<R, B>> implements Branch<R> {

        /**
         * The output value produced by this branch
         */
        final R output;

        /**
         * Indicates if the current branch condition was met.
         */
        final boolean branchHit;

        BaseBranch(boolean branchHit, R output) {
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
        public B consume(Consumer<? super R> consumer) {
            if (branchHit) consumer.accept(output);
            return self();
        }

        @Override
        public <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return self();
        }

        /**
         * Returns the result of the current branch.
         * <p>
         * Use {@link #getAsType()} to specify the desired type when needed.
         *
         * @return the result of the current branch
         */
        @Override
        public R get() {
            return output;
        }

        /**
         * Processes the branch's input and output using the given BiConsumer if the condition is satisfied.
         *
         * @param consumer the BiConsumer that processes the branch's input and output
         * @return the current branch instance
         */
        public B consume(BiConsumer<? super T, ? super R> consumer) {
            if (branchHit) consumer.accept(input, output);
            return self();
        }

        /**
         * Returns the current branch instance itself, optimizing for type inference and performance.
         *
         * @return the current instance with inferred type
         */
        B self() {
            return uncheckedCast(this);
        }

        /**
         * Maps the branch's input and output to a new type if the condition is satisfied.
         *
         * @param function the mapping function
         * @param <O>      the type of the mapped output
         * @return a branch instance with the new output type
         */
        public abstract <O> Branch<O> map(BiFunction<? super T, ? super R, ? extends O> function);

    }

    /**
     * Defines operations that can be performed on a branch.
     *
     * @param <T> the type of output value for this branch
     */
    private interface Branch<T> extends MultiOutputSupplier<T> {

        /**
         * Runs the specified action if the branch condition is satisfied.
         *
         * @param runnable the action to execute
         * @return the current branch instance
         */
        Branch<T> run(Runnable runnable);

        /**
         * Processes the branch's output using the given consumer if the condition is satisfied.
         *
         * @param consumer the consumer that processes the branch's output
         * @return the current branch instance
         */
        Branch<T> consume(Consumer<? super T> consumer);

        /**
         * Maps the branch's output to a new type if the condition is satisfied.
         *
         * @param function the mapping function
         * @param <O>      the type of the mapped output
         * @return a branch instance with the new output type
         */
        <O> Branch<O> map(Function<? super T, ? extends O> function);

        /**
         * Throws an exception if the branch condition is satisfied.
         *
         * @param supplier the supplier that provides the exception to throw
         * @param <X>      the type of exception to be thrown
         * @return the current branch instance for method chaining
         * @throws X the exception thrown if the condition is met
         */
        <X extends Throwable> Branch<T> throwIt(Supplier<? extends X> supplier) throws X;
    }

}


