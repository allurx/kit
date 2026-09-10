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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Shares backend access, typed conversions, and comparison flow.
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

    /**
     * Parses each input once, stopping at the first mismatch.
     *
     * @param jsons the JSON strings to compare
     * @param parser parses a JSON value
     * @param equality compares two parsed values
     * @param <N> the parsed value type
     * @return whether all values are equivalent
     * @throws IllegalArgumentException if no JSON strings are supplied
     */
    protected final <N> boolean compare(String[] jsons, Function<String, N> parser, BiPredicate<N, N> equality) {
        if (jsons.length == 0) {
            throw new IllegalArgumentException("At least one JSON string is required");
        }
        N first = parser.apply(jsons[0]);
        return IntStream.range(1, jsons.length).allMatch(i -> equality.test(first, parser.apply(jsons[i])));
    }
}
