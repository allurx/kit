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

/**
 * Diagnostic failure from waiting for an ATTACH process to expose its debugging port.
 * {@link Chrome.ChromeBuilder#build()} retains this exception as the cause of its {@link BrowserException}.
 * Failures produced by the startup helper include recent output when available and may carry suppressed cleanup
 * exceptions. An interrupted wait retains its {@link InterruptedException} as the cause. The public constructor
 * accepts a message without collecting diagnostics or performing cleanup.
 *
 * @author allurx
 */
public class BrowserStartupFailureException extends BrowserException {

    /**
     * Creates a startup failure with the supplied diagnostic message.
     *
     * @param message the detail message
     */
    public BrowserStartupFailureException(String message) {
        super(message);
    }
}

