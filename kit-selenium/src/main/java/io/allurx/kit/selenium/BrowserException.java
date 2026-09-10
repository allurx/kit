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
 * Unchecked failure reported by Kit's browser lifecycle API.
 * {@link Chrome.ChromeBuilder#build()} wraps configuration, launch, and session construction failures
 * in this type while preserving their causes. Operations invoked directly on the returned WebDriver
 * and {@link Chrome#close()} retain their underlying exception types.
 *
 * @author allurx
 */
public class BrowserException extends RuntimeException {

    /**
     * Constructs a new {@code BrowserException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BrowserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code BrowserException} with the specified detail message.
     *
     * @param message the detail message
     */
    public BrowserException(String message) {
        super(message);
    }
}

