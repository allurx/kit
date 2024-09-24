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
package red.zyc.kit.base.concurrency;

import red.zyc.kit.base.function.MultiOutputSupplier;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author allurx
 */
public interface Poller {

    <A, B> PollResult<B> poll(A input,
                              Function<? super A, ? extends B> function,
                              Predicate<? super B> predicate);

    default <T> void poll(T input,
                             Consumer<? super T> consumer,
                             BooleanSupplier booleanSupplier) {
        poll(() -> consumer.accept(input), booleanSupplier);
    }

    default void poll(Runnable runnable, BooleanSupplier booleanSupplier) {
        poll(null, unused -> {
            runnable.run();
            return null;
        }, unused -> booleanSupplier.getAsBoolean());
    }

    /**
     * @param count  轮询次数
     * @param result 轮询结果
     * @param <T>    轮询结果类型
     */
    record PollResult<T>(int count, T result) implements MultiOutputSupplier<T> {

        @Override
        public T get() {
            return result;
        }
    }

}
