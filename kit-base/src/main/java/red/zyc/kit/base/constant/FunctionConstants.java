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

import static red.zyc.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * A collection of commonly used function constants.
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
     * A {@link Predicate} that always returns {@code true}.
     */
    public static final Predicate<?> TRUE_PREDICATE = x -> true;

    /**
     * A {@link Predicate} that always returns {@code false}.
     */
    public static final Predicate<?> FALSE_PREDICATE = x -> false;

    /**
     * Returns a {@link Predicate} that always evaluates to true.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that always returns true
     */
    public static <T> Predicate<? super T> truePredicate() {
        return uncheckedCast(TRUE_PREDICATE);
    }

    /**
     * Returns a {@link Predicate} that always evaluates to false.
     *
     * @param <T> the type of the input to the predicate
     * @return a predicate that always returns false
     */
    public static <T> Predicate<? super T> falsePredicate() {
        return uncheckedCast(FALSE_PREDICATE);
    }
}

