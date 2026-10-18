/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */

package io.allurx.kit.base.function;

import java.util.function.Supplier;

/**
 * A {@link Supplier} whose result is a {@link Throwable}, for lazily creating exceptions.
 * Returning an exception does not throw it; {@link #get()} does not declare checked exceptions.
 *
 * @param <T> the type of {@link Throwable} to be supplied
 * @author allurx
 */
@FunctionalInterface
public interface ThrowableSupplier<T extends Throwable> extends Supplier<T> {
}
