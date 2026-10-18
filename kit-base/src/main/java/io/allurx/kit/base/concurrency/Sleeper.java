/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
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
