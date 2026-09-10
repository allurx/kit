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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a Chrome browser instance that can be controlled via WebDriver.
 * <p>
 * This class allows you to start and manage a Chrome browser instance, either by attaching to an
 * already running instance or by starting a new instance directly from WebDriver.
 * </p>
 *
 * @author allurx
 */
public final class Chrome implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Chrome.class);

    /**
     * The WebDriver session owned by this instance.
     */
    private final WebDriver webDriver;

    /**
     * The process started in ATTACH mode, or null when WebDriver manages the browser in HOSTED mode.
     */
    private final Process process;

    /**
     * Takes ownership of the resources created by one successful build.
     *
     * @param webDriver the established WebDriver session
     * @param process the process started in ATTACH mode, or null in HOSTED mode
     */
    private Chrome(WebDriver webDriver, Process process) {
        this.webDriver = webDriver;
        this.process = process;
    }

    /**
     * Returns the {@link WebDriver} instance controlling the Chrome browser.
     *
     * @return the {@link WebDriver} instance
     */
    public WebDriver webDriver() {
        return webDriver;
    }

    /**
     * Closes the Chrome browser and terminates any running processes.
     * This method ensures that both the {@link WebDriver} instance and the Chrome process are properly closed.
     */
    @Override
    public void close() {
        try {
            Optional.ofNullable(webDriver).ifPresent(WebDriver::quit);
        } finally {
            Optional.ofNullable(process).ifPresent(Process::destroy);
        }
    }

    /**
     * Creates a new {@link ChromeBuilder} to construct a {@link Chrome} instance.
     *
     * @return a new {@link ChromeBuilder}
     */
    public static ChromeBuilder builder() {
        return new ChromeBuilder();
    }

    /**
     * A builder class for constructing a {@link Chrome} instance.
     * <p>
     * This builder allows configuration of Chrome startup parameters, communication modes, and more.
     * It can be reused to create independent browser instances. Each instance must be closed separately.
     * Configuration is mutable, so a builder must not be used concurrently without external synchronization.
     * </p>
     *
     * @author allurx
     */
    public static class ChromeBuilder {

        private Mode mode;
        private String chromePath;

        /**
         * Browser arguments for future builds. Executable paths and generated debugging ports are added
         * only to the command for an individual attempt so failures and retries cannot change this list.
         *
         * @see <a href="https://peter.sh/experiments/chromium-command-line-switches/">chromium-command-line-switches</a>
         */
        private final List<String> arguments = new ArrayList<>(List.of(
                "--no-first-run",
                "--start-maximized",
                "--disable-extensions",
                "--disable-gpu",
                "--disable-software-rasterizer",
                "--disable-background-networking",
                "--disable-sync",
                "--disable-translate",
                "--disable-renderer-backgrounding",
                "--disable-client-side-phishing-detection",
                "--disable-hang-monitor",
                "--disable-audio-output",
                "--disable-accelerated-2d-canvas",
                "--enable-low-end-device-mode",
                "--enable-simple-cache-backend",
                "--disable-quic",
                "--disable-infobars",
                "--disable-session-crashed-bubble",
                "--disable-speech-api",
                "--disable-save-password-bubble",
                "--disable-notifications"
        ));

        /**
         * Creates an independent builder with the default browser arguments.
         */
        public ChromeBuilder() {
        }

        /**
         * Adds Chrome startup arguments.
         *
         * @param args Chrome startup arguments
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder addArgs(String... args) {
            arguments.addAll(Arrays.asList(args));
            return this;
        }

        /**
         * Removes Chrome startup arguments.
         *
         * @param args Chrome startup arguments
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder removeArgs(String... args) {
            arguments.removeAll(Arrays.asList(args));
            return this;
        }

        /**
         * Sets the communication mode between {@link WebDriver} and the browser.
         *
         * @param mode the communication mode, one of {@link Mode}
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the path to the Chrome executable.
         *
         * @param chromePath the absolute path to the Chrome binary
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder chromePath(String chromePath) {
            this.chromePath = chromePath;
            return this;
        }

        /**
         * Finds an available port on the local machine.
         * <p>
         * This port is used for remote debugging when {@link Mode#ATTACH} is selected.
         * </p>
         *
         * @return a random available port
         */
        private int findAvailablePort() {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                // Port number 0 means the OS will assign a random available port
                return serverSocket.getLocalPort();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        /**
         * Starts a process with the given {@link ProcessBuilder}.
         * <p>
         * This method is used to start the Chrome process and wraps any checked exceptions.
         * </p>
         *
         * @param processBuilder the {@link ProcessBuilder} to use
         * @return the started {@link Process}
         */
        private Process startProcess(ProcessBuilder processBuilder) {
            try {
                return processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Constructs and returns a {@link Chrome} instance based on the builder configuration.
         * Each successful call creates a separate session without changing the builder's configuration
         * or any previously returned instance. The caller owns and must close each returned instance.
         * <p>
         * In {@link Mode#ATTACH} mode, waits up to three seconds for the debugging port to accept
         * a connection before creating the WebDriver session. Output is drained in the background
         * and a bounded tail is included in startup failures. If the process exits, the port does
         * not become ready, or startup is interrupted, the process is terminated and a
         * {@link BrowserStartupFailureException} is preserved as the cause of the construction
         * exception. Interruption also preserves the calling thread's interrupt flag.
         * If WebDriver session construction fails after the port is ready, the newly started
         * process is terminated and its output pipe is closed, preserving the original cause.
         * </p>
         *
         * @return a new {@link Chrome} instance
         * @throws BrowserException if Chrome fails to start or an error occurs during construction
         */
        public Chrome build() {
            try {
                Optional.ofNullable(mode).orElseThrow(() -> new IllegalStateException("Chrome mode not set"));
                Optional.ofNullable(chromePath).orElseThrow(() -> new IllegalStateException("Chrome Path not set"));
                return switch (mode) {
                    case ATTACH -> {

                        // Start the Chrome process
                        int port = findAvailablePort();
                        var command = new ArrayList<>(arguments);
                        command.addFirst(chromePath);
                        command.add("--remote-debugging-port=" + port);

                        // First, start Chrome so that WebDriver can later establish a connection with it.
                        // Note: For the same Chrome startup commands with --user-data-dir or --profile-directory.
                        // For example:
                        // chrome --user-data-dir=path1 --remote-debugging-port=1
                        // chrome --user-data-dir=path1 --remote-debugging-port=2
                        // Even if two different port numbers are specified, only one Chrome process will be started,
                        // and one of the ports will inevitably fail to bind. Subsequently, WebDriver will not be able to
                        // establish a connection with Chrome.
                        // Summary:
                        // At any given time, there will be only one Chrome process with the same --user-data-dir or
                        // --profile-directory due to the design of Chrome itself.
                        var process = startProcess(new ProcessBuilder(command).redirectErrorStream(true));
                        ChromeStartup.await(process, port, Duration.ofSeconds(3));

                        try {
                            // Transfer process ownership only after the WebDriver session is established.
                            var options = new ChromeOptions();
                            options.setBinary(chromePath);
                            options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + port);
                            yield new Chrome(new ChromeDriver(options), process);
                        } catch (Throwable failure) {
                            ChromeStartup.terminate(process, failure);
                            throw failure;
                        }
                    }
                    case HOSTED -> {
                        var options = new ChromeOptions()
                                .setBinary(chromePath)
                                .addArguments(arguments);
                        yield new Chrome(new ChromeDriver(options), null);
                    }
                };
            } catch (Throwable t) {
                throw new BrowserException("Chrome construction failed", t);
            }
        }
    }
}

