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
package red.zyc.kit.base.constant;

import java.util.function.Predicate;

/**
 * A collection of commonly used function constants.
 * <p>This class provides static constants for commonly used functional interfaces, such as {@link Runnable} and {@link Predicate}, to avoid creating new instances repeatedly.</p>
 *
 * @author allurx
 */
public final class FunctionConstants {

    private FunctionConstants() {
    }

    /**
     * A {@link Runnable} that performs no action.
     * <p>This constant is useful as a no-op placeholder in scenarios where a {@link Runnable} is required but no action is needed.</p>
     */
    public static final Runnable EMPTY_RUNNABLE = () -> {
    };

    /**
     * A {@link Predicate} that always returns {@code true}.
     * <p>This constant can be used in contexts where a {@link Predicate} is needed that should always evaluate to {@code true}.</p>
     */
    public static final Predicate<?> TRUE_PREDICATE = x -> true;

    /**
     * A {@link Predicate} that always returns {@code false}.
     * <p>This constant can be used in contexts where a {@link Predicate} is needed that should always evaluate to {@code false}.</p>
     */
    public static final Predicate<?> FALSE_PREDICATE = x -> false;

}
