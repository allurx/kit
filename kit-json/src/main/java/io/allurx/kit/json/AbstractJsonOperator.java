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
 * Shares backend access, class-to-token delegation, and short-circuit comparison flow.
 * Subclasses implement the backend's serialization, conversion, and equality rules.
 *
 * @param <J> the backend type
 * @author allurx
 */
public abstract class AbstractJsonOperator<J> implements JsonOperator<J> {

    /**
     * The retained backend reference. A final reference does not make a custom backend immutable.
     */
    protected final J subject;

    /**
     * Creates an operator around an existing backend.
     *
     * @param subject the backend, never null
     * @throws NullPointerException if the backend is null
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
     * Parses the first input and compares each subsequent input with it until a mismatch occurs.
     * A single input is parsed and then returns true without invoking the equality predicate.
     * Exceptions from parsing or comparison propagate to the caller.
     *
     * @param jsons the JSON strings to compare
     * @param parser parses each visited JSON input once
     * @param equality compares the first parsed value with a subsequent parsed value
     * @param <N> the parsed value type
     * @return whether all values are equivalent
     * @throws IllegalArgumentException if no JSON strings are supplied
     * @throws NullPointerException if the input array is null
     */
    protected final <N> boolean compare(String[] jsons, Function<String, N> parser, BiPredicate<N, N> equality) {
        if (jsons.length == 0) {
            throw new IllegalArgumentException("At least one JSON string is required");
        }
        N first = parser.apply(jsons[0]);
        return IntStream.range(1, jsons.length).allMatch(i -> equality.test(first, parser.apply(jsons[i])));
    }
}
