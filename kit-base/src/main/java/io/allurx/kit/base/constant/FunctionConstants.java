/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
package io.allurx.kit.base.constant;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static io.allurx.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * Shared, stateless functions. The predicates accept any input, including null.
 *
 * @author allurx
 */
public final class FunctionConstants {

    private FunctionConstants() {
    }

    /**
     * A {@link Runnable} that performs no action.
     */
    public static final Runnable EMPTY_RUNNABLE = () -> {
    };

    /**
     * A {@link BooleanSupplier} that always returns {@code true}.
     */
    public static final BooleanSupplier TRUE_SUPPLIER = () -> true;

    /**
     * A {@link BooleanSupplier} that always returns {@code false}.
     */
    public static final BooleanSupplier FALSE_SUPPLIER = () -> false;

    /**
     * A {@link Predicate} that always returns {@code true}.
     */
    public static final Predicate<?> TRUE_PREDICATE = x -> true;

    /**
     * A {@link Predicate} that always returns {@code false}.
     */
    public static final Predicate<?> FALSE_PREDICATE = x -> false;

    /**
     * Returns the shared true predicate with a type compatible with the caller's input.
     *
     * @param <T> the type of the input to the predicate
     * @return {@link #TRUE_PREDICATE} typed for the caller
     */
    public static <T> Predicate<? super T> truePredicate() {
        return uncheckedCast(TRUE_PREDICATE);
    }

    /**
     * Returns the shared false predicate with a type compatible with the caller's input.
     *
     * @param <T> the type of the input to the predicate
     * @return {@link #FALSE_PREDICATE} typed for the caller
     */
    public static <T> Predicate<? super T> falsePredicate() {
        return uncheckedCast(FALSE_PREDICATE);
    }
}

