/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
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

