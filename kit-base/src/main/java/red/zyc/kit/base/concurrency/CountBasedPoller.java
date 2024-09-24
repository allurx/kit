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

/**
 * @author allurx
 */
public class CountBasedPoller extends AbstractPoller<CountBasedPoller> {

    /**
     * 函数最多能够执行的次数
     */
    protected int count;

    @Override
    public <A, B> PollResult<B> poll(A input,
                                     Function<? super A, ? extends B> function,
                                     Predicate<? super B> predicate) {
        int cnt = 0;
        B result = null;
        for (int i = 0; i < count; i++) {
            cnt++;
            if (predicate.test(result = execute(input, function))) break;
        }
        return new PollResult<>(cnt, result);
    }

    public CountBasedPoller count(int count) {
        this.count = count;
        return this;
    }
}
