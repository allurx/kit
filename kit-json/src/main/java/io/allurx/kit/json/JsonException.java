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

