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
package red.zyc.kit.base.poller;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * An interface for controlling thread sleep behavior.
 *
 * <p>Implementations of this interface can customize how a thread is put to sleep, which can be useful for controlling timing and pauses in various logic scenarios.</p>
 *
 * @author allurx
 */
public interface Sleeper {

    /**
     * The default {@link Sleeper} implementation.
     * <p>This implementation uses {@link LockSupport#parkNanos(long)} to pause the thread for the specified duration.</p>
     */
    Sleeper DEFAULT = duration -> LockSupport.parkNanos(duration.toNanos());

    /**
     * Causes the current thread to sleep for the specified duration.
     *
     * @param duration the amount of time for which the thread should sleep
     */
    void sleep(Duration duration);
}
