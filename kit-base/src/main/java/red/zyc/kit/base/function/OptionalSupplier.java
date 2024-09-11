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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A functional interface that provides an {@link Optional} result.<br>
 * This interface extends {@link Supplier} to return an {@link Optional} instead of a raw value,
 * allowing for the explicit handling of missing values.
 *
 * @param <T> the type of the {@link Optional} result
 * @author allurx
 */
@FunctionalInterface
public interface OptionalSupplier<T> extends Supplier<Optional<T>> {
}
