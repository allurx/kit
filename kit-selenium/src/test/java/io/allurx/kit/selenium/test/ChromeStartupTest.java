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
package io.allurx.kit.selenium.test;

import io.allurx.kit.selenium.BrowserStartupFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies bounded Chrome startup waiting, process output handling, and failure cleanup.
 * <p>
 * Tests use local Java child processes, loopback sockets, and a controlled process substitute.
 * They do not require an installed Chrome browser or establish a ChromeDriver session.
 * </p>
 *
 * @author allurx
 */
@Timeout(value = 15, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class ChromeStartupTest {

    /**
     * Creates a test instance whose child processes are owned and cleaned up by each test.
     */
    public ChromeStartupTest() {
    }

    /**
     * Verifies that a startup timeout terminates its process tree without affecting an unrelated process.
     *
     * @throws Exception if process setup, port allocation, or cleanup fails
     */
    @Test
    void timesOutWithoutWaitingForAnAliveProcessToCloseItsOutput() throws Exception {
        int port = availablePort();
        var unrelated = startProcess("idle", port);
        try (var tree = startProcessTree()) {
            long started = System.nanoTime();
            assertThrows(BrowserStartupFailureException.class,
                    () -> awaitStartup(tree.root(), port, Duration.ofMillis(500)));
            assertTrue(Duration.ofNanos(System.nanoTime() - started).compareTo(Duration.ofSeconds(3)) < 0,
                    "Startup failure must not wait for the child process to close its output");
            tree.assertExited();
            assertTrue(unrelated.isAlive(), "Cleanup must only affect the failed startup's process tree");
        } finally {
            terminate(unrelated);
        }
    }

    /**
     * Verifies that repeated port checks detect delayed readiness and leave the successful process running.
     *
     * @throws Exception if process setup, port allocation, or cleanup fails
     */
    @Test
    void detectsAPortThatBecomesReadyAfterStartupBegins() throws Exception {
        int port = availablePort();
        var process = startProcess("late", port);
        try {
            awaitStartup(process, port, Duration.ofSeconds(5));
            assertTrue(process.isAlive(), "A successful startup must keep the browser process running");
        } finally {
            terminate(process);
        }
    }

    /**
     * Verifies that large output without newlines is drained while only a bounded diagnostic tail is retained.
     *
     * @throws Exception if process setup, port allocation, or cleanup fails
     */
    @Test
    void drainsLargeOutputWithoutNewlinesAndKeepsDiagnosticsBounded() throws Exception {
        int port = availablePort();
        var process = startProcess("flood", port);
        try {
            var failure = assertThrows(BrowserStartupFailureException.class,
                    () -> awaitStartup(process, port, Duration.ofSeconds(5)));
            assertTrue(failure.getMessage().contains("end-of-large-output"),
                    "Both output pipes must be drained so the child can reach its final diagnostic");
            assertTrue(failure.getMessage().contains("17"), "An early exit must report its exit code");
            assertTrue(failure.getMessage().length() <= 9_000, "Captured startup diagnostics must be bounded");
            assertTrue(process.waitFor(3, TimeUnit.SECONDS));
        } finally {
            terminate(process);
        }
    }

    /**
     * Verifies that a child exiting before port readiness reports its exit code and diagnostic output.
     *
     * @throws Exception if process setup, port allocation, or cleanup fails
     */
    @Test
    void reportsAnEarlyExitWithItsCodeAndOutput() throws Exception {
        int port = availablePort();
        var process = startProcess("exit", port);
        try {
            var failure = assertThrows(BrowserStartupFailureException.class,
                    () -> awaitStartup(process, port, Duration.ofSeconds(5)));
            assertTrue(failure.getMessage().contains("7"), "An early exit must report its exit code");
            assertTrue(failure.getMessage().contains("startup failure marker"));
            assertFalse(process.isAlive());
        } finally {
            terminate(process);
        }
    }

    /**
     * Verifies that interruption stops startup promptly, terminates the child, and preserves the flag and cause.
     *
     * @throws Exception if process setup, thread coordination, port allocation, or cleanup fails
     */
    @Test
    void respondsToInterruptionAndPreservesTheInterruptFlagAndCause() throws Exception {
        int port = availablePort();
        try (var tree = startProcessTree()) {
            var started = new CountDownLatch(1);
            var failure = new AtomicReference<Throwable>();
            var interrupted = new AtomicBoolean();
            var worker = Thread.ofPlatform().daemon().unstarted(() -> {
                started.countDown();
                try {
                    awaitStartup(tree.root(), port, Duration.ofSeconds(10));
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    interrupted.set(Thread.currentThread().isInterrupted());
                }
            });
            try {
                worker.start();
                assertTrue(started.await(3, TimeUnit.SECONDS));
                worker.interrupt();
                worker.join(3_000);
                assertFalse(worker.isAlive(), "Interrupted startup must return promptly");
                var startupFailure = assertInstanceOf(BrowserStartupFailureException.class, failure.get());
                assertInstanceOf(InterruptedException.class, startupFailure.getCause());
                assertTrue(interrupted.get(), "Startup must preserve the caller's interrupt flag");
                tree.assertExited();
            } finally {
                worker.interrupt();
                worker.join(3_000);
            }
        }
    }

    @Test
    void terminatesTheProcessTreeAfterDriverConstructionFailure() throws Exception {
        try (var tree = startProcessTree()) {
            var cause = new IllegalStateException("driver session creation failed");
            var failure = new BrowserStartupFailureException("ChromeDriver construction failed");
            failure.initCause(cause);

            assertDoesNotThrow(() -> terminateAfterFailure(tree.root(), failure));

            tree.assertExited();
            assertEquals("ChromeDriver construction failed", failure.getMessage());
            assertSame(cause, failure.getCause());
            assertEquals(0, failure.getSuppressed().length);
        }
    }

    @Test
    void keepsTheProcessTreeAliveAfterSuccessfulStartup() throws Exception {
        try (var tree = startProcessTree();
             var socket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))) {
            awaitStartup(tree.root(), socket.getLocalPort(), Duration.ofSeconds(5));

            assertTrue(tree.owned().stream().allMatch(ProcessHandle::isAlive),
                    "Successful startup must keep its owned process tree alive");
        }
    }

    @Test
    void terminatesObservedDescendantsAfterTheRootExits() throws Exception {
        try (var tree = startProcessTree()) {
            var process = exitsAfterDescendantsObserved(tree.root());

            var failure = assertThrows(BrowserStartupFailureException.class,
                    () -> awaitStartup(process, availablePort(), Duration.ofSeconds(5)));

            assertTrue(failure.getMessage().contains("Chrome exited with code"));
            tree.assertExited();
        }
    }

    /**
     * Verifies that cleanup starts closing an output pipe without making startup failure wait for close to finish.
     *
     * @throws Exception if controlled stream coordination or cleanup fails
     */
    @Test
    void closesAnInheritedOutputPipeWithoutWaitingForCloseToFinish() throws Exception {
        var readStarted = new CountDownLatch(1);
        var closeStarted = new CountDownLatch(1);
        var allowClose = new CountDownLatch(1);
        var closed = new CountDownLatch(1);
        // Model a pipe retained after process exit: reading and closing remain blocked until the test releases close.
        var output = new InputStream() {
            @Override
            public int read() throws IOException {
                readStarted.countDown();
                awaitLatch(closed);
                return -1;
            }

            @Override
            public void close() throws IOException {
                closeStarted.countDown();
                awaitLatch(allowClose);
                closed.countDown();
            }
        };
        try (output) {
            try {
                var process = exitedProcess(output);
                long started = System.nanoTime();
                assertThrows(BrowserStartupFailureException.class,
                        () -> awaitStartup(process, 0, Duration.ofSeconds(3)));
                assertTrue(Duration.ofNanos(System.nanoTime() - started).compareTo(Duration.ofSeconds(3)) < 0,
                        "Startup failure must not wait for output close to finish");
                assertTrue(readStarted.await(3, TimeUnit.SECONDS), "The diagnostic reader must have started");
                assertTrue(closeStarted.await(3, TimeUnit.SECONDS), "Failed startup must close its output pipe");
                assertEquals(1, closed.getCount(), "The stream must still be waiting for the test to release close");
            } finally {
                // Release the controlled close before automatic resource cleanup starts.
                allowClose.countDown();
            }
        } finally {
            assertTrue(closed.await(3, TimeUnit.SECONDS));
        }
    }

    /**
     * Creates an already exited process substitute whose output stream remains independently controlled by the test.
     *
     * @param output the process output stream to expose without closing it on process termination
     * @return a process substitute that has exited with code {@code 7}
     */
    private static Process exitedProcess(InputStream output) {
        return new Process() {
            @Override
            public OutputStream getOutputStream() {
                return OutputStream.nullOutputStream();
            }

            @Override
            public InputStream getInputStream() {
                return output;
            }

            @Override
            public InputStream getErrorStream() {
                return InputStream.nullInputStream();
            }

            @Override
            public int waitFor() {
                return 7;
            }

            @Override
            public int exitValue() {
                return 7;
            }

            @Override
            public void destroy() {
                // No operating system process exists; the test controls the stream's lifetime separately.
            }
        };
    }

    /**
     * Waits for a controlled stream event, translating interruption into an I/O failure while preserving its flag.
     *
     * @param latch the event to wait for
     * @throws IOException if the waiting thread is interrupted
     */
    private static void awaitLatch(CountDownLatch latch) throws IOException {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException(exception);
        }
    }

    /**
     * Invokes the package-private startup helper through the module opening configured for these tests.
     * <p>
     * Invocation exceptions are unwrapped so assertions observe the helper's original failure type and cause.
     * </p>
     *
     * @param process the started or controlled process whose startup is being checked
     * @param port the loopback debugging port to probe
     * @param timeout the maximum duration allowed for port readiness
     * @throws BrowserStartupFailureException if startup fails, times out, or is interrupted
     * @throws AssertionError if reflection fails or the helper throws an unexpected checked exception
     */
    private static void awaitStartup(Process process, int port, Duration timeout) {
        try {
            var method = Class.forName("io.allurx.kit.selenium.ChromeStartup")
                    .getDeclaredMethod("await", Process.class, int.class, Duration.class);
            method.setAccessible(true);
            method.invoke(null, process, port, timeout);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (exception.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new AssertionError(exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static void terminateAfterFailure(Process process, Throwable failure) throws Exception {
        var method = Class.forName("io.allurx.kit.selenium.ChromeStartup")
                .getDeclaredMethod("terminate", Process.class, Throwable.class);
        method.setAccessible(true);
        method.invoke(null, process, failure);
    }

    private static Process exitsAfterDescendantsObserved(Process root) {
        return new Process() {
            @Override
            public OutputStream getOutputStream() {
                return root.getOutputStream();
            }

            @Override
            public InputStream getInputStream() {
                return root.getInputStream();
            }

            @Override
            public InputStream getErrorStream() {
                return root.getErrorStream();
            }

            @Override
            public int waitFor() throws InterruptedException {
                return root.waitFor();
            }

            @Override
            public int exitValue() {
                return root.exitValue();
            }

            @Override
            public void destroy() {
                root.destroy();
            }

            @Override
            public Process destroyForcibly() {
                root.destroyForcibly();
                return this;
            }

            @Override
            public ProcessHandle toHandle() {
                return root.toHandle();
            }

            @Override
            public Stream<ProcessHandle> descendants() {
                try (var descendants = root.descendants()) {
                    var snapshot = descendants.toList();
                    // Once the root exits, only the retained snapshot can identify its reparented descendants.
                    terminate(root);
                    return snapshot.stream();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
            }
        };
    }

    private static ProcessTree startProcessTree() throws Exception {
        var root = startProcess("tree", 2);
        var owned = new ArrayList<ProcessHandle>();
        owned.add(root.toHandle());
        try {
            var javaCommand = root.toHandle().info().command().orElseThrow();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                try (var descendants = root.descendants()) {
                    descendants.filter(handle -> !owned.contains(handle)).forEach(owned::add);
                }
                // Windows also attaches console hosts; readiness depends on the three Java nodes.
                long javaProcesses = owned.stream()
                        .filter(handle -> handle.info().command().filter(javaCommand::equals).isPresent()).count();
                if (javaProcesses == 3 && owned.stream().allMatch(ProcessHandle::isAlive)) {
                    return new ProcessTree(root, List.copyOf(owned));
                }
                Thread.sleep(10);
            }
            throw new AssertionError("The process fixture must start both child and grandchild before startup checks");
        } catch (Throwable failure) {
            try (var tree = new ProcessTree(root, owned)) {
                throw failure;
            }
        }
    }

    private record ProcessTree(Process root, List<ProcessHandle> owned) implements AutoCloseable {
        void assertExited() throws Exception {
            for (var handle : owned) {
                handle.onExit().get(3, TimeUnit.SECONDS);
                assertFalse(handle.isAlive(), "Failed startup must terminate owned process " + handle.pid());
            }
        }

        @Override
        public void close() throws Exception {
            for (var handle : owned.reversed()) {
                if (handle.isAlive()) {
                    handle.destroyForcibly();
                }
            }
            assertExited();
        }
    }

    /**
     * Obtains an operating system assigned loopback port and releases the temporary reservation.
     *
     * @return the port selected for a subsequent child process to bind
     * @throws Exception if the loopback address cannot be resolved or the temporary socket cannot be opened or closed
     */
    private static int availablePort() throws Exception {
        try (var socket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }

    /**
     * Launches {@link FakeChrome} with the current JDK and merges stderr into stdout as the production launcher does.
     *
     * @param mode the child behavior: {@code idle}, {@code late}, {@code flood}, {@code exit}, or {@code tree}
     * @param port the loopback port used by {@code late}, or the descendant depth used by {@code tree}
     * @return the child process, which the caller must clean up
     * @throws Exception if the compiled test location cannot be resolved or the child process cannot be started
     */
    private static Process startProcess(String mode, int port) throws Exception {
        return FakeChrome.processBuilder(mode, port)
                .redirectErrorStream(true)
                .start();
    }

    /**
     * Terminates the specific child created by a test and verifies that it exits within three seconds.
     *
     * @param process the child process owned by the test
     * @throws InterruptedException if the cleanup wait is interrupted
     * @throws AssertionError if the child process does not exit within the cleanup deadline
     */
    private static void terminate(Process process) throws InterruptedException {
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        assertTrue(process.waitFor(3, TimeUnit.SECONDS), "The test must clean up its own child process");
    }

    /**
     * Provides controlled process and socket behavior for startup regression tests.
     * <p>
     * This class runs in a separate JVM using only JDK APIs, so its process pipes and lifetime are real
     * while its behavior remains independent of Chrome, ChromeDriver, and external services.
     * </p>
     *
     * @author allurx
     */
    public static class FakeChrome {

        /**
         * Creates the process fixture; subprocess execution uses {@link #main(String[])}.
         */
        public FakeChrome() {
        }

        /**
         * Runs the requested child process behavior.
         * <p>
         * {@code idle} keeps the process and its output open; {@code late} opens the loopback port after a delay;
         * {@code flood} writes large output without newlines and exits with code {@code 17};
         * {@code exit} writes a diagnostic marker and exits with code {@code 7};
         * {@code tree} creates the requested depth of descendants sharing its output pipes.
         * </p>
         *
         * @param args the behavior mode followed by the loopback port or descendant depth
         * @throws Exception if the arguments are invalid, the socket cannot be opened or closed, or a wait is interrupted
         */
        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "idle" -> Thread.sleep(30_000);
                case "tree" -> {
                    int depth = Integer.parseInt(args[1]);
                    if (depth > 0) {
                        processBuilder("tree", depth - 1).inheritIO().start();
                    }
                    Thread.sleep(30_000);
                }
                case "late" -> {
                    Thread.sleep(400);
                    try (var socket = new ServerSocket(Integer.parseInt(args[1]), 50,
                            InetAddress.getByName("127.0.0.1"))) {
                        Thread.sleep(30_000);
                    }
                }
                case "flood" -> {
                    String chunk = "x".repeat(1_024);
                    for (int i = 0; i < 256; i++) {
                        System.out.print(chunk);
                    }
                    System.out.flush();
                    System.err.print("end-of-large-output");
                    System.err.flush();
                    System.exit(17);
                }
                case "exit" -> {
                    System.err.print("startup failure marker");
                    System.err.flush();
                    System.exit(7);
                }
                default -> throw new IllegalArgumentException(args[0]);
            }
        }

        private static ProcessBuilder processBuilder(String mode, int argument) throws Exception {
            Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java");
            if (!Files.isExecutable(javaExecutable)) {
                javaExecutable = javaExecutable.resolveSibling("java.exe");
            }
            String classes = Path.of(FakeChrome.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toString();
            return new ProcessBuilder(javaExecutable.toString(), "-cp", classes, FakeChrome.class.getName(),
                    mode, Integer.toString(argument));
        }
    }
}
