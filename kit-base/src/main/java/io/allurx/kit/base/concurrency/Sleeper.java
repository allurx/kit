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
package io.allurx.kit.base.concurrency;

import java.time.Duration;

/**
 * Supplies a replaceable delay operation, used by {@link IntervalBasedPoller} between attempts.
 * Custom implementations can advance a test clock instead of blocking the current thread.
 * Implementations used for polling should preserve the interrupt flag when a wait is interrupted.
 *
 * @author allurx
 */
public interface Sleeper {

    /**
     * Pauses using {@link Thread#sleep(Duration)} to avoid returning early due to park permits or spurious returns from parking.
     * Interruption ends the wait and restores the interrupt flag for the caller.
     * Timing is subject to system timer precision and thread scheduling.
     */
    Sleeper DEFAULT = duration -> {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    };

    /**
     * Waits for the requested duration according to this implementation's timing policy.
     * No checked interruption exception is declared; {@link #DEFAULT} restores the thread's interrupt flag.
     *
     * @param duration the requested delay; pollers supply a non-null, non-negative duration
     */
    void sleep(Duration duration);
}
