/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
package io.allurx.kit.selenium;

/**
 * Unchecked failure reported by Kit's browser lifecycle API.
 * {@link Chrome.ChromeBuilder#build()} wraps configuration, launch, and session construction failures
 * in this type while preserving their causes. Operations invoked directly on the returned WebDriver
 * and {@link Chrome#close()} retain their underlying exception types. Errors propagate without wrapping.
 *
 * @author allurx
 */
public class BrowserException extends RuntimeException {

    /**
     * Creates a browser failure with contextual detail and its original cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BrowserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a browser failure without an initial cause.
     *
     * @param message the detail message
     */
    public BrowserException(String message) {
        super(message);
    }
}

