/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Typed JSON serialization, conversion, and structural comparison through {@link io.allurx.kit.json.JsonOperator}.
 * {@link io.allurx.kit.json.JacksonOperator} and {@link io.allurx.kit.json.GsonOperator} retain their
 * backend's configuration and document the differences in parsing, comparison, and exception handling.
 * The default operators are shared; derive a separate backend for local configuration.
 *
 * @author allurx
 */
package io.allurx.kit.json;
