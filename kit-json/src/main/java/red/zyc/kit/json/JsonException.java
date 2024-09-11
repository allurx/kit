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

package red.zyc.kit.json;

/**
 * Exception for JSON errors.
 *
 * @author allurx
 */
public class JsonException extends RuntimeException {

    /**
     * Constructor.
     *
     * @param message The error message
     * @param t       The cause of the exception, typically a {@link Throwable}
     */
    public JsonException(String message, Throwable t) {
        super(message, t);
    }
}

