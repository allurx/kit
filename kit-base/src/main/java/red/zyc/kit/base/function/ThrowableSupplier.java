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

package red.zyc.kit.base.function;

import java.util.function.Supplier;

/**
 * A functional interface for supplying {@link Throwable} instances.<br>
 * This interface extends {@link Supplier} to provide a {@link Throwable} instead of a regular value.
 * It is useful when you need a supplier that can produce exceptions.
 *
 * @param <T> the type of {@link Throwable} to be supplied
 * @author allurx
 */
@FunctionalInterface
public interface ThrowableSupplier<T extends Throwable> extends Supplier<T> {
}
