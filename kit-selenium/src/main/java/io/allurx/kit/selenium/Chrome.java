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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Default Chrome startup arguments.
     *
     * @see <a href="https://peter.sh/experiments/chromium-command-line-switches/">chromium-command-line-switches</a>
     */
    private final List<String> defaultArgs = Arrays.stream(new String[]{
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
            "--disable-notifications",
    }).collect(Collectors.toList());

    private Chrome() {
    }

    private Mode mode;
    private String chromePath;
    private WebDriver webDriver;
    private Process process;

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
        return new ChromeBuilder(new Chrome());
    }

    /**
     * A builder class for constructing a {@link Chrome} instance.
     * <p>
     * This builder allows configuration of Chrome startup parameters, communication modes, and more.
     * </p>
     */
    public static class ChromeBuilder {

        private final Chrome chrome;

        /**
         * Constructs a new {@code ChromeBuilder} with the specified {@link Chrome} instance.
         *
         * @param chrome the {@link Chrome} instance to be used by this builder
         */
        public ChromeBuilder(Chrome chrome) {
            this.chrome = chrome;
        }

        /**
         * Adds Chrome startup arguments.
         *
         * @param args Chrome startup arguments
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder addArgs(String... args) {
            chrome.defaultArgs.addAll(Arrays.asList(args));
            return this;
        }

        /**
         * Removes Chrome startup arguments.
         *
         * @param args Chrome startup arguments
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder removeArgs(String... args) {
            chrome.defaultArgs.removeAll(Arrays.asList(args));
            return this;
        }

        /**
         * Sets the communication mode between {@link WebDriver} and the browser.
         *
         * @param mode the communication mode, one of {@link Mode}
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder mode(Mode mode) {
            chrome.mode = mode;
            return this;
        }

        /**
         * Sets the path to the Chrome executable.
         *
         * @param chromePath the absolute path to the Chrome binary
         * @return the current ChromeBuilder instance for chaining
         */
        public ChromeBuilder chromePath(String chromePath) {
            chrome.chromePath = chromePath;
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
                Optional.ofNullable(chrome.mode).orElseThrow(() -> new IllegalStateException("Chrome mode not set"));
                Optional.ofNullable(chrome.chromePath).orElseThrow(() -> new IllegalStateException("Chrome Path not set"));
                switch (chrome.mode) {
                    case ATTACH -> {

                        // Start the Chrome process
                        int port = findAvailablePort();
                        chrome.defaultArgs.addFirst(chrome.chromePath);
                        chrome.defaultArgs.add("--remote-debugging-port=" + port);

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
                        var process = startProcess(new ProcessBuilder(chrome.defaultArgs).redirectErrorStream(true));
                        ChromeStartup.await(process, port, Duration.ofSeconds(3));

                        try {
                            // Transfer process ownership only after the WebDriver session is established.
                            var options = new ChromeOptions();
                            options.setBinary(chrome.chromePath);
                            options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + port);
                            chrome.webDriver = new ChromeDriver(options);
                            chrome.process = process;
                        } catch (Throwable failure) {
                            ChromeStartup.terminate(process, failure);
                            throw failure;
                        }
                    }
                    case HOSTED -> {
                        var options = new ChromeOptions()
                                .setBinary(chrome.chromePath)
                                .addArguments(chrome.defaultArgs);
                        chrome.webDriver = new ChromeDriver(options);
                    }
                }
            } catch (Throwable t) {
                throw new BrowserException("Chrome construction failed", t);
            }
            return chrome;
        }
    }
}

