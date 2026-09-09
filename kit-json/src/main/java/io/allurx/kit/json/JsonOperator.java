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

import com.google.gson.Gson;
import io.allurx.kit.base.reflection.TypeToken;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * JSON operations with access to an immutable, reusable backend.
 *
 * @param <J> the backend type, such as {@link JsonMapper} or {@link Gson}
 * @author allurx
 */
public interface JsonOperator<J> extends JsonOperation {

    /**
     * Returns the backend used by this operator.
     *
     * @return the JSON backend
     */
    J subject();

    /**
     * Creates a new operator around the supplied backend without copying it.
     *
     * @param supplier supplies the backend
     * @return a new operator
     */
    JsonOperator<J> with(Supplier<J> supplier);

    /**
     * Creates a new operator around the transformed backend.
     * Use {@code with(mapper -> mapper.rebuild().enable(...).build())} for Jackson or
     * {@code with(gson -> gson.newBuilder().setPrettyPrinting().create())} for Gson.
     *
     * @param unaryOperator transforms the existing backend into the desired backend
     * @return a new operator; the original operator keeps its backend
     */
    JsonOperator<J> with(UnaryOperator<J> unaryOperator);

    /**
     * Copies properties using the backend's in-memory conversion into a dynamically supplied type.
     * Use the class or type-token overload for a statically typed result.
     *
     * @param source the source object
     * @param type the target type
     * @return the converted object
     */
    Object copyProperties(Object source, Type type);

    /**
     * Copies properties into an object of the specified class.
     *
     * @param source the source object
     * @param type the target class
     * @param <T> the target type
     * @return the converted object
     */
    <T> T copyProperties(Object source, Class<T> type);

    /**
     * Copies properties into an object of the type captured by the token.
     *
     * @param source the source object
     * @param typeToken the target type token
     * @param <T> the target type
     * @return the converted object
     */
    <T> T copyProperties(Object source, TypeToken<T> typeToken);

    /**
     * Compares JSON values structurally, ignoring object property order and preserving array order.
     * Numeric equality follows the concrete operator's comparison rules.
     *
     * @param jsons the JSON strings to compare; must contain at least one element
     * @return whether all supplied JSON values are equivalent
     * @throws IllegalArgumentException if no JSON strings are supplied
     */
    boolean compare(String... jsons);

    /**
     * A shared operator using Jackson's native defaults and built-in Java time support.
     */
    JacksonOperator JACKSON_OPERATOR = new JacksonOperator(JsonMapper.builder().build());

    /**
     * A shared operator using Gson's native defaults and built-in Java time support.
     */
    GsonOperator GSON_OPERATOR = new GsonOperator(new Gson());
}
