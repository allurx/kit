/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
package io.allurx.kit.base.reflection;

/**
 * Utilities for unchecked type casts.
 *
 * @author allurx
 */
public final class TypeConverter {

    private TypeConverter() {
    }

    /**
     * Returns the same object as the caller's target type without validating it.
     * <p>
     * The caller must ensure type compatibility. An incompatible value may cause a
     * {@link ClassCastException} when the result is used.
     *
     * @param a   the source object, which may be null
     * @param <A> the source type
     * @param <B> the target type
     * @return the same object as the target type, or null if the source is null
     */
    @SuppressWarnings("unchecked")
    public static <A, B> B uncheckedCast(A a) {
        return (B) a;
    }
}

