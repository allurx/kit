/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */

package io.allurx.kit.json;

/**
 * Wraps Jackson serialization, deserialization, comparison, and conversion failures
 * while preserving the original cause. Gson operations retain Gson's exception types.
 *
 * @author allurx
 */
public class JsonException extends RuntimeException {

    /**
     * Creates a JSON failure with its original diagnostic context.
     *
     * @param message the error message, possibly null
     * @param t the original cause, possibly null
     */
    public JsonException(String message, Throwable t) {
        super(message, t);
    }
}

