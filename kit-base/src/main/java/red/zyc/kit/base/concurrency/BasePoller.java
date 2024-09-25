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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static red.zyc.kit.base.reflection.TypeConverter.uncheckedCast;

/**
 * @author allurx
 */
public abstract class BasePoller implements Poller {

    protected final System.Logger logger;
    protected final List<Class<? extends Throwable>> ignoredExceptions;

    protected BasePoller(List<Class<? extends Throwable>> ignoredExceptions, System.Logger logger) {
        this.logger = Optional.ofNullable(logger).orElse(System.getLogger(getClass().getName()));
        this.ignoredExceptions = Optional.ofNullable(ignoredExceptions).orElse(new ArrayList<>());
    }

    public <A, B> void check(Function<? super A, ? extends B> function, Predicate<? super B> predicate) {
        Objects.requireNonNull(function, "The Function cannot be null");
        Objects.requireNonNull(predicate, "The Predicate used to test the output of the Function cannot be null");
    }

    /**
     * 执行函数
     */
    protected <A, B> B execute(A input, Function<? super A, ? extends B> function) {
        try {
            return function.apply(input);
        } catch (Throwable t) {
            if (ignoredExceptions.stream().noneMatch(ignoredException -> ignoredException.isInstance(t))) throw t;
            logger.log(System.Logger.Level.WARNING, "Poller is ignoring the exception: {0}", t.getClass().getName());
            return null;
        }
    }

    public static class BasePollerBuilder<B extends BasePollerBuilder<B>> {

        protected System.Logger logger;
        protected List<Class<? extends Throwable>> ignoredExceptions = new ArrayList<>();

        public B ignoreExceptions(Class<? extends Throwable> ignoredException) {
            this.ignoredExceptions.add(Objects.requireNonNull(ignoredException, "The Ignore Exception cannot be null"));
            return uncheckedCast(this);
        }

        public B ignoreExceptions(List<Class<? extends Throwable>> ignoredExceptions) {
            this.ignoredExceptions = Objects.requireNonNull(ignoredExceptions, "The List of Ignore Exceptions cannot be null");
            return uncheckedCast(this);
        }

        public B logger(System.Logger logger) {
            this.logger = Objects.requireNonNull(logger, "The Logger cannot be null");
            return uncheckedCast(this);
        }
    }

}