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
 * Startup is bounded using monotonic elapsed time. Merged process output is drained on a
 * virtual thread, with only its recent tail retained for failure diagnostics. Output reading
 * continues after successful startup so a full pipe cannot block the running browser.
 * </p>
 *
 * @author allurx
 * @see Chrome.ChromeBuilder#build()
 */
final class ChromeStartup {

    /**
     * Logger for unexpected failures when reading or closing process output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeStartup.class);

    /**
     * Maximum pause between probes and upper bound used to calculate each connection timeout.
     */
    private static final long POLL_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

    /**
     * Maximum number of UTF-16 code units retained from recent process output.
     */
    private static final int OUTPUT_LIMIT = 8192;

    private static final Duration TERMINATION_TIMEOUT = Duration.ofSeconds(1);

    /**
     * Prevents instantiation of this startup utility.
     */
    private ChromeStartup() {
    }

    /**
     * Waits until the debugging port accepts a connection while the process is still alive.
     * <p>
     * The caller must merge stderr into stdout before starting the process. A startup failure
     * requests termination of that process and its observed descendants, with at most one second
     * of exit waiting. Its output pipe is closed asynchronously without waiting for EOF.
     * Interruption is preserved on the calling thread and recorded as the cause of the startup
     * exception. A successful return leaves the process and output reader running.
     * </p>
     *
     * @param process the already started process owned by the caller, with merged stderr and stdout
     * @param port the local debugging port to probe
     * @param timeout the positive startup timeout, representable in nanoseconds
     * @return descendants observed during startup, retained for cleanup if session construction fails
     * @throws BrowserStartupFailureException if the process exits, the port does not become ready
     *                                       within the timeout, or the calling thread is interrupted
     */
    static Set<ProcessHandle> await(Process process, int port, Duration timeout) {
        long started = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        var descendants = new HashSet<ProcessHandle>();
        var output = new Output();
        // Keep draining after startup as well: a full pipe can block the browser later.
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
                        return Set.copyOf(descendants);
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
            terminate(process, descendants, failure);
            throw failure;
        } catch (RuntimeException e) {
            terminate(process, descendants, e);
            throw e;
        }
    }

    /**
     * Probes the local debugging port using a connection timeout derived from the remaining time.
     * <p>
     * The socket timeout has millisecond precision and is capped at one polling interval.
     * A successful probe establishes TCP reachability only; WebDriver creates its session later.
     * </p>
     *
     * @param port the local debugging port
     * @param remainingNanos the positive time remaining before the startup deadline
     * @return {@code true} if a connection succeeds, otherwise {@code false}
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
     * Terminates a failed construction's process and descendants still associated with it.
     *
     * @param process the process created by the failed construction attempt
     * @param failure the original construction failure to receive suppressed termination errors
     */
    static void terminate(Process process, Throwable failure) {
        terminate(process, Set.of(), failure);
    }

    /**
     * Retains observed descendants across parent exit, then snapshots the remaining tree before killing the root.
     * Cleanup waits at most one second for exit, preserves interruption, and suppresses errors onto the original failure.
     * Process snapshots cannot capture children that detach before observation or appear after enumeration.
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
            // Descendants may keep the pipe open. Closing it must not delay startup failure.
            Thread.ofVirtual().name("kit-chrome-output-close").start(() -> {
                try {
                    process.getInputStream().close();
                } catch (IOException e) {
                    LOGGER.warn("Unable to close Chrome process output", e);
                }
            });
        }
    }

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

        /**
         * Creates an initially empty diagnostic buffer.
         */
        private Output() {
        }

        /**
         * Continuously drains merged output until EOF or stream closure.
         * <p>
         * Reads fixed-size character blocks rather than lines, so output without newlines neither
         * blocks diagnostic collection nor requires an unbounded line buffer. Read errors are
         * logged while the process is alive; closure after process termination is expected.
         * </p>
         *
         * @param process the process whose merged output is read on the background thread
         */
        private void read(Process process) {
            // The process is launched with stderr merged into stdout.
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
         * Appends decoded output and discards the oldest characters beyond {@link ChromeStartup#OUTPUT_LIMIT}.
         *
         * @param buffer the buffer containing decoded output
         * @param length the number of characters to append from the start of the buffer
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
