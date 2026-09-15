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
package io.allurx.kit.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Monitors the debugging port of a Chrome process created by {@link Chrome.ChromeBuilder}.
 * <p>
 * Port readiness uses a deadline based on monotonic elapsed time. Merged process output is drained on a
 * virtual thread, with only its recent tail retained for failure diagnostics. Output reading
 * continues after successful startup until EOF or an I/O failure so normal output does not fill the pipe.
 * </p>
 *
 * @author allurx
 * @see Chrome.ChromeBuilder#build()
 */
final class ChromeStartup {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeStartup.class);

    /**
     * Maximum pause between probes and upper bound used to calculate each connection timeout.
     */
    private static final long POLL_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

    /**
     * Maximum number of UTF-16 code units retained from recent process output.
     */
    private static final int OUTPUT_LIMIT = 8192;

    /**
     * Shared exit-wait budget for the root process and all observed descendants after termination requests.
     */
    private static final Duration TERMINATION_TIMEOUT = Duration.ofSeconds(1);

    private ChromeStartup() {
    }

    /**
     * Waits until the loopback debugging port accepts a TCP connection while the process is still alive.
     * The probe does not verify the endpoint's identity or establish a ChromeDriver session.
     * <p>
     * The caller must merge stderr into stdout before starting the process and owns cleanup on any failure.
     * Observed descendant handles are added to the supplied set for cleanup if readiness or later session
     * construction fails. Use {@link #terminate(Process, Set, Throwable)} with that set and the original failure.
     * Interruption is preserved on the calling thread and recorded as the cause of the startup
     * exception. A successful return leaves the process and output reader running.
     * The readiness deadline excludes the brief wait for final output after process exit.
     * </p>
     *
     * @param process the already started process owned by the caller, with merged stderr and stdout
     * @param port the local debugging port to probe
     * @param timeout the positive startup timeout, representable in nanoseconds
     * @param descendants the mutable set that receives observed descendants for caller-owned failure cleanup;
     *                    snapshots may omit descendants created or detached between observations
     * @throws BrowserStartupFailureException if the process exits, the port does not become ready
     *                                       within the timeout, or the calling thread is interrupted
     */
    static void await(Process process, int port, Duration timeout, Set<ProcessHandle> descendants) {
        long started = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        var output = new Output();
        var reader = Thread.ofVirtual().name("kit-chrome-output").start(() -> output.read(process));
        try {
            while (true) {
                if (process.isAlive()) {
                    // Keep handles even if a launcher exits and its children lose their parent association.
                    try (var current = process.descendants()) {
                        current.forEach(descendants::add);
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Chrome startup interrupted");
                }
                if (!process.isAlive()) {
                    // An inherited pipe can outlive its process; never wait for EOF indefinitely.
                    reader.join(Duration.ofMillis(100));
                    throw output.failure("Chrome exited with code " + process.exitValue());
                }
                long remaining = timeoutNanos - (System.nanoTime() - started);
                if (remaining <= 0) {
                    throw output.failure("Chrome debugging port " + port + " was not ready within " + timeout);
                }
                if (isPortOpen(port, remaining)) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Chrome startup interrupted");
                    }
                    if (System.nanoTime() - started < timeoutNanos && process.isAlive()) {
                        return;
                    }
                }
                remaining = timeoutNanos - (System.nanoTime() - started);
                if (remaining > 0) {
                    TimeUnit.NANOSECONDS.sleep(Math.min(remaining, POLL_INTERVAL_NANOS));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            var failure = output.failure("Chrome startup interrupted");
            failure.initCause(e);
            throw failure;
        }
    }

    /**
     * Rounds the remaining time down to milliseconds, then clamps it between one millisecond and one polling interval.
     * The caller rechecks the monotonic deadline after a successful probe.
     */
    private static boolean isPortOpen(int port, long remainingNanos) {
        int timeoutMillis = (int) Math.max(1, TimeUnit.NANOSECONDS.toMillis(
                Math.min(remainingNanos, POLL_INTERVAL_NANOS)));
        try (var socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), timeoutMillis);
            return true;
        } catch (IOException e) {
            // A refused or timed-out connection is expected while the debugging port starts.
            return false;
        }
    }

    /**
     * Merges previously observed descendants with current snapshots before requesting forced termination.
     * The supplied set is copied, preserving handles whose parent has already exited.
     * <p>
     * A shared one-second exit-wait budget starts after termination requests. Cleanup preserves interruption
     * and attaches caught runtime exceptions and exit timeouts to the original failure as suppressed exceptions.
     * Asynchronous output-close I/O errors are logged separately. Neither process enumeration nor termination
     * is atomic: descendants that detach before observation or appear after enumeration may escape cleanup.
     * </p>
     *
     * @param process the root process owned by the failed construction attempt
     * @param observed descendants retained during startup, including any whose parent has already exited
     * @param failure the original failure to receive suppressed cleanup errors and exit timeouts
     */
    static void terminate(Process process, Set<ProcessHandle> observed, Throwable failure) {
        var descendants = new HashSet<>(observed);
        try {
            try (var current = process.descendants()) {
                current.forEach(descendants::add);
            } catch (RuntimeException e) {
                suppress(failure, e);
            }
            // Known children may have acquired descendants since the last startup probe.
            for (var descendant : List.copyOf(descendants)) {
                try (var current = descendant.descendants()) {
                    current.forEach(descendants::add);
                } catch (RuntimeException e) {
                    suppress(failure, e);
                }
            }
            try {
                process.destroyForcibly();
            } catch (RuntimeException e) {
                suppress(failure, e);
            }
            for (var descendant : descendants) {
                try {
                    descendant.destroyForcibly();
                } catch (RuntimeException e) {
                    suppress(failure, e);
                }
            }
            awaitTermination(process, descendants, failure);
        } finally {
            closeOutputAsync(process);
        }
    }

    private static void closeOutputAsync(Process process) {
        // Descendants may keep the pipe open. Closing it must not delay startup failure.
        Thread.ofVirtual().name("kit-chrome-output-close").start(() -> {
            try {
                process.getInputStream().close();
            } catch (IOException e) {
                LOGGER.warn("Unable to close Chrome process output", e);
            }
        });
    }

    /**
     * Waits within a single shared deadline, removing exited descendants from the supplied mutable set.
     * Interruption is temporarily cleared so cleanup can finish, then restored before returning.
     * A timeout is recorded on the original failure instead of replacing it.
     */
    private static void awaitTermination(Process process, Set<ProcessHandle> descendants, Throwable failure) {
        long started = System.nanoTime();
        boolean interrupted = Thread.interrupted();
        try {
            while (true) {
                descendants.removeIf(descendant -> !descendant.isAlive());
                if (!process.isAlive() && descendants.isEmpty()) {
                    return;
                }
                long remaining = TERMINATION_TIMEOUT.toNanos() - (System.nanoTime() - started);
                if (remaining <= 0) {
                    failure.addSuppressed(new IllegalStateException(
                            "Chrome process tree did not exit within " + TERMINATION_TIMEOUT));
                    return;
                }
                try {
                    TimeUnit.NANOSECONDS.sleep(Math.min(remaining, POLL_INTERVAL_NANOS));
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } catch (RuntimeException e) {
            suppress(failure, e);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // A process implementation may rethrow the original failure; self-suppression would replace it with another error.
    private static void suppress(Throwable failure, RuntimeException error) {
        if (error != failure) {
            failure.addSuppressed(error);
        }
    }

    /**
     * Retains a bounded tail of process output for startup failure diagnostics.
     * <p>
     * The reader thread appends output while the startup thread takes diagnostic snapshots.
     * Both operations synchronize on this instance; blocking stream reads never hold that monitor.
     * </p>
     *
     * @author allurx
     */
    private static final class Output {

        /**
         * Recent process output, guarded by this instance's monitor.
         */
        private final StringBuilder tail = new StringBuilder();

        private Output() {
        }

        /**
         * Drains merged output until EOF or an I/O failure, using the process reader's native encoding.
         * <p>
         * Reads fixed-size character blocks so diagnostics do not depend on line boundaries or require an unbounded
         * line buffer. Read errors are logged while the process is alive; closure after process termination is expected.
         * </p>
         *
         * @param process the process whose merged output is read on the background thread
         */
        private void read(Process process) {
            try (var reader = process.inputReader()) {
                char[] buffer = new char[1024];
                int length;
                while ((length = reader.read(buffer)) != -1) {
                    append(buffer, length);
                }
            } catch (IOException e) {
                if (process.isAlive()) {
                    LOGGER.warn("Unable to read Chrome process output", e);
                }
            }
        }

        /**
         * Discards the oldest characters beyond {@link ChromeStartup#OUTPUT_LIMIT} after appending decoded output.
         */
        private synchronized void append(char[] buffer, int length) {
            tail.append(buffer, 0, length);
            if (tail.length() > OUTPUT_LIMIT) {
                tail.delete(0, tail.length() - OUTPUT_LIMIT);
            }
        }

        /**
         * Creates a startup exception containing a consistent snapshot of the retained output.
         *
         * @param message the reason startup failed
         * @return a new exception with recent output included when available
         */
        private synchronized BrowserStartupFailureException failure(String message) {
            return new BrowserStartupFailureException(tail.isEmpty() ? message
                    : message + System.lineSeparator() + "Recent Chrome output:" + System.lineSeparator() + tail);
        }
    }
}
