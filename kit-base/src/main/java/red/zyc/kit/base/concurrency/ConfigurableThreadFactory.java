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

import red.zyc.kit.base.ConditionalFlow;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A configurable thread factory that creates threads with a custom name prefix.
 * The factory supports both virtual and platform threads.
 * <p>
 * The name prefix is required and cannot be empty or null. The thread names
 * are generated with the specified prefix followed by a unique number.
 * </p>
 * <p>
 * This class implements the {@link ThreadFactory} interface, which provides
 * a method to create new threads.
 * </p>
 *
 * @author allurx
 */
public class ConfigurableThreadFactory implements ThreadFactory {

    private final boolean virtual;
    private final String threadNamePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(0);

    /**
     * Constructs a {@code ConfigurableThreadFactory} with the specified virtual
     * thread flag and thread name prefix.
     *
     * @param virtual          a boolean indicating whether to create virtual threads
     * @param threadNamePrefix the prefix to use for thread names; cannot be null or empty
     * @throws IllegalArgumentException if {@code threadNamePrefix} is null or empty
     */
    public ConfigurableThreadFactory(boolean virtual, String threadNamePrefix) {
        this.virtual = virtual;
        this.threadNamePrefix = ConditionalFlow.<String>when(threadNamePrefix == null || threadNamePrefix.isBlank())
                .throwException(() -> new IllegalArgumentException("Thread name prefix cannot be empty or null."))
                .orElse().yield(() -> threadNamePrefix)
                .get()
                .orElseThrow();
    }

    /**
     * Creates a new thread with the specified task.
     * The thread is created as a virtual thread if {@code virtual} is true,
     * otherwise, a platform thread is created.
     *
     * @param task the task to be executed by the new thread
     * @return a new thread with the specified task and a name based on the prefix
     */
    @Override
    public Thread newThread(Runnable task) {
        return virtual ? Thread.ofVirtual()
                .name(threadNamePrefix, this.threadNumber.getAndIncrement())
                .unstarted(task) :
                Thread.ofPlatform()
                        .name(threadNamePrefix, this.threadNumber.getAndIncrement())
                        .unstarted(task);
    }
}
