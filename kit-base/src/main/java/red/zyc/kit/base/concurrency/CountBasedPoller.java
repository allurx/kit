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

/**
 * @author allurx
 */
public class CountBasedPoller<A, B> extends AbstractPoller<A, B, CountBasedPoller<A, B>> {

    /**
     * 函数最多能够执行的次数
     */
    protected int count;

    @Override
    public B polling() {
        B output = null;
        for (int i = 0; i < count; i++) {
            output = execute();
            if (predicate.test(output)) return output;
        }
        return output;
    }

    public CountBasedPoller<A, B> count(int count) {
        this.count = count;
        return this;
    }
}
