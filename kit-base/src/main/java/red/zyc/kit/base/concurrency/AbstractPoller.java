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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author allurx
 */
public abstract class AbstractPoller<A, B, S extends AbstractPoller<A, B, S>> implements Poller<A, B> {

    A input;
    B output;
    @SuppressWarnings("unchecked")
    final S self = (S) this;
    protected Function<? super A, ? extends B> function;
    protected Predicate<? super B> predicate;
    protected List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();
    protected final System.Logger LOGGER = System.getLogger(getClass().getName());

    @Override
    public B get() {
        Objects.requireNonNull(function, "The Function cannot be null");
        Objects.requireNonNull(predicate, "The predicate used to test the output of the Function cannot be null");
        return polling();
    }

    @Override
    public Poller<A, B> apply(A input, Function<? super A, ? extends B> function) {
        this.input = input;
        this.function = function;
        return this;
    }

    @Override
    public Poller<A, B> until(Predicate<? super B> predicate) {
        this.predicate = predicate;
        return this;
    }

    public S ignoreExceptions(Class<? extends Throwable> ignoredException) {
        this.ignoredExceptions.add(ignoredException);
        return self;
    }

    public S ignoreExceptions(List<Class<? extends Throwable>> ignoredExceptions) {
        this.ignoredExceptions = ignoredExceptions;
        return self;
    }

    abstract B polling();

    /**
     * 执行函数
     */
    protected B execute() {
        try {
            return function.apply(input);
        } catch (Throwable t) {
            if (ignoredExceptions.stream().noneMatch(ignoredException -> ignoredException.isInstance(t))) throw t;
            LOGGER.log(System.Logger.Level.WARNING, "Poller is ignoring the exception: {0}", t.getClass().getName());
            return null;
        }
    }

}
