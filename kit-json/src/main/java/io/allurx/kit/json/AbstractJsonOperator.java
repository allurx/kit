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

import io.allurx.kit.base.reflection.TypeToken;

import java.util.Objects;

/**
 * Holds a JSON backend and implements the class-based convenience operations.
 *
 * @param <J> the backend type
 * @author allurx
 */
public abstract class AbstractJsonOperator<J> implements JsonOperator<J> {

    /**
     * The immutable backend used by this operator.
     */
    protected final J subject;

    /**
     * Creates an operator around an existing backend.
     *
     * @param subject the backend, never null
     */
    protected AbstractJsonOperator(J subject) {
        this.subject = Objects.requireNonNull(subject, "subject");
    }

    @Override
    public J subject() {
        return subject;
    }

    @Override
    public <T> T fromJsonString(String json, Class<T> type) {
        return fromJsonString(json, TypeToken.of(type));
    }

    @Override
    public <T> T copyProperties(Object source, Class<T> type) {
        return copyProperties(source, TypeToken.of(type));
    }
}
