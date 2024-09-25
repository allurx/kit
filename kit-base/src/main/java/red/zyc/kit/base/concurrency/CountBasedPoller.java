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

import java.util.function.Function;
import java.util.function.Predicate;

import static red.zyc.kit.base.ConditionalFlow.when;

/**
 * @author allurx
 */
public class CountBasedPoller extends BasePoller {

    /**
     * 函数最多能够执行的次数
     */
    private final int count;

    private CountBasedPoller(CountBasedPollerBuilder builder) {
        super(builder.ignoredExceptions, builder.logger);
        this.count = builder.count;
    }

    @Override
    public <A, B> PollResult<B> poll(A input,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        check(function, predicate);
        int cnt = 0;
        B result = null;
        for (int i = 0; i < count; i++) {
            cnt++;
            if (predicate.test(result = execute(input, function))) break;
        }
        return new PollResult<>(cnt, result);
    }

    public static CountBasedPollerBuilder builder() {
        return new CountBasedPollerBuilder();
    }

    public static class CountBasedPollerBuilder extends BasePollerBuilder<CountBasedPollerBuilder> {

        private int count;

        public CountBasedPollerBuilder count(int count) {
            return when(count > 0).run(() -> this.count = count).supply(() -> this)
                    .orElse().throwIt(() -> new IllegalArgumentException("The maximum number of polling attempts must be greater than 0. Provided value: %s".formatted(count)))
                    .getAsType();
        }

        public CountBasedPoller build() {
            return new CountBasedPoller(this);
        }
    }

}
