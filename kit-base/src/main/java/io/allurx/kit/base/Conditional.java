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
 * Evaluates an {@code if / else if / else} chain as its methods are called.
 * Conditions and actions run immediately; retrieving a result does not execute the chain again.
 *
 * <p>Usage example:
 * <pre>{@code
 * Conditional.of(7)
 *         .when(value -> value < 0).map(value -> "negative")
 *         .elseIf(value -> value == 0).map(value -> "zero")
 *         .orElse().map(value -> "positive")
 *         .get(); // "positive"
 * }</pre>
 *
 * <p>Once a branch matches, later {@code elseIf} conditions and later branch actions
 * are skipped. Predicates receive the original input, including {@code null}.
 * Callback exceptions propagate to the caller; skipped callbacks are not dereferenced.
 *
 * <p>Branch type parameters describe callback inputs when that branch executes.
 * Results use {@link Object}: a skipped mapping can retain the original input or
 * an earlier branch's result. {@code getAsType()} is an explicit unchecked cast.
 *
 * <p>Use a fresh instance for each chain. Branches share mutable match state with their
 * enclosing instance, so restarting or forking a chain does not create independent state.
 * Instances and their branches are not thread-safe.
 *
 * @param <I> the type of the input value
 * @author allurx
 */
public final class Conditional<I> {

    private final I input;

    /**
     * Shared by every branch created from this instance, including mapped branches.
     */
    private boolean hit = false;

    private Conditional(I input) {
        this.input = input;
    }

    /**
     * Creates a new {@code Conditional} instance with the specified input.
     *
     * @param input the original input, which may be null
     * @param <I>   the type of input value
     * @return a new {@code Conditional} instance
     */
    public static <I> Conditional<I> of(I input) {
        return new Conditional<>(input);
    }

    /**
     * Creates the initial branch from an already evaluated condition.
     * Unlike {@code elseIf}, this method does not consult earlier match state.
     *
     * @param condition the initial condition to evaluate
     * @return an {@link IfBranch} instance for further branching
     */
    public IfBranch<I> when(boolean condition) {
        return new IfBranch<>(condition, input);
    }

    /**
     * Evaluates the supplier once and creates the initial branch.
     *
     * @param booleanSupplier the supplier that provides the boolean condition
     * @return an {@link IfBranch} instance for further branching
     * @throws NullPointerException if booleanSupplier is null
     */
    public IfBranch<I> when(BooleanSupplier booleanSupplier) {
        return when(booleanSupplier.getAsBoolean());
    }

    /**
     * Evaluates the predicate once against the original input and creates the initial branch.
     *
     * @param predicate the predicate used to evaluate the input
     * @return an {@link IfBranch} instance for further branching
     * @throws NullPointerException if predicate is null
     */
    public IfBranch<I> when(Predicate<? super I> predicate) {
        return when(predicate.test(input));
    }

    /**
     * Represents the "if" branch of the conditional flow.
     *
     * @param <O> the callback input type when this branch executes
     */
    public class IfBranch<O> extends DerivableBranch<O, IfBranch<O>> {

        IfBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        /**
         * Maps this branch's value if it matched; otherwise retains its stored value.
         *
         * @param function the mapping function, evaluated only for a matching branch
         * @param <U> the callback input type after a successful mapping
         * @return a new branch with the mapped value, or this skipped branch
         * @throws NullPointerException if this branch matched and function is null
         */
        @Override
        public <U> IfBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new IfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

    }

    /**
     * Represents an "else if" branch in the conditional flow.
     *
     * @param <O> the callback input type when this branch executes
     */
    public class ElseIfBranch<O> extends DerivableBranch<O, ElseIfBranch<O>> {

        ElseIfBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        /**
         * Maps this branch's value if it matched; otherwise retains its stored value.
         *
         * @param function the mapping function, evaluated only for a matching branch
         * @param <U> the callback input type after a successful mapping
         * @return a new branch with the mapped value, or this skipped branch
         * @throws NullPointerException if this branch matched and function is null
         */
        @Override
        public <U> ElseIfBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new ElseIfBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }

    }

    /**
     * Represents the "else" branch in the conditional flow.
     *
     * @param <O> the callback input type when this branch executes
     */
    public class ElseBranch<O> extends BaseBranch<O, ElseBranch<O>> {

        ElseBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        /**
         * Maps this branch's value if no earlier branch matched; otherwise retains the earlier result.
         *
         * @param function the mapping function, evaluated only for a matching branch
         * @param <U> the callback input type after a successful mapping
         * @return a new branch with the mapped value, or this skipped branch
         * @throws NullPointerException if this branch matched and function is null
         */
        @Override
        public <U> ElseBranch<U> map(Function<? super O, ? extends U> function) {
            return branchHit ? new ElseBranch<>(true, function.apply(output)) : uncheckedCast(this);
        }
    }

    /**
     * Base class for branches that can derive "else if" or "else" branches.
     *
     * @param <O> the callback input type when this branch executes
     * @param <B> the type of derived branch
     */
    private abstract class DerivableBranch<O, B extends DerivableBranch<O, B>> extends BaseBranch<O, B> {

        DerivableBranch(boolean branchHit, O output) {
            super(branchHit, output);
        }

        /**
         * Evaluates the supplier only if no earlier branch matched.
         * A matching branch starts with the original input; a skipped branch retains this branch's result.
         *
         * @param booleanSupplier the supplier providing the condition
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         * @throws NullPointerException if no earlier branch matched and booleanSupplier is null
         */
        public ElseIfBranch<I> elseIf(BooleanSupplier booleanSupplier) {
            return new ElseIfBranch<>(!hit && booleanSupplier.getAsBoolean(), getAsType());
        }

        /**
         * Tests the original input only if no earlier branch matched.
         * A matching branch starts with that input; a skipped branch retains this branch's result.
         *
         * @param predicate the predicate that tests the input
         * @return an {@link ElseIfBranch} instance to continue the conditional flow
         * @throws NullPointerException if no earlier branch matched and predicate is null
         */
        public ElseIfBranch<I> elseIf(Predicate<? super I> predicate) {
            return new ElseIfBranch<>(!hit && predicate.test(input), getAsType());
        }

        /**
         * Matches the original input if no earlier branch matched, otherwise retains this branch's result.
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
     * @param <O> the callback input type when this branch executes
     * @param <B> the type of this branch
     */
    private abstract class BaseBranch<O, B extends BaseBranch<O, B>> implements Branch<O> {

        /**
         * A skipped map can retain a value unrelated to its inferred callback type.
         */
        final O output;

        final boolean branchHit;

        BaseBranch(boolean branchHit, O output) {
            if (branchHit) hit = true;
            this.branchHit = branchHit;
            this.output = output;
        }

        /**
         * Executes the action if this branch matched.
         *
         * @param runnable the action to execute
         * @return this branch for chaining
         * @throws NullPointerException if this branch matched and runnable is null
         */
        @Override
        public B run(Runnable runnable) {
            if (branchHit) runnable.run();
            return self();
        }

        /**
         * Passes the stored value to the consumer if this branch matched.
         *
         * @param consumer the output processor
         * @return this branch for chaining
         * @throws NullPointerException if this branch matched and consumer is null
         */
        @Override
        public B consume(Consumer<? super O> consumer) {
            if (branchHit) consumer.accept(output);
            return self();
        }

        /**
         * Creates and throws the supplied exception if this branch matched.
         *
         * @param supplier the supplier for the exception
         * @param <X> the exception type
         * @return this branch when it did not match
         * @throws X the supplied exception if this branch matched
         * @throws NullPointerException if this branch matched and supplier or its result is null
         */
        @Override
        public <X extends Throwable> B throwIt(Supplier<? extends X> supplier) throws X {
            if (branchHit) throw supplier.get();
            return self();
        }

        /**
         * Returns this branch's stored result, which may come from the original input or an earlier branch.
         * Later mappings do not replace this stored result.
         *
         * @return the result of the current branch
         */
        @Override
        public Object get() {
            return output;
        }

        B self() {
            return uncheckedCast(this);
        }
    }

    /**
     * Defines operations that can be performed on a branch.
     *
     * @param <O> the callback input type when this branch executes
     */
    private interface Branch<O> extends MultiOutputSupplier<Object> {

        Branch<O> run(Runnable runnable);

        Branch<O> consume(Consumer<? super O> consumer);

        /**
         * Maps the output to a new, possibly null value if the condition is met.
         * A successful mapping creates a new branch and leaves this branch's value intact.
         * A skipped mapping preserves the stored value even when its type differs from {@code U}.
         *
         * @param function the mapping function
         * @param <U>      the new output type
         * @return a new matching branch, or the same skipped branch
         * @throws NullPointerException if this branch matched and function is null
         */
        <U> Branch<U> map(Function<? super O, ? extends U> function);

        <X extends Throwable> Branch<O> throwIt(Supplier<? extends X> supplier) throws X;
    }

}



