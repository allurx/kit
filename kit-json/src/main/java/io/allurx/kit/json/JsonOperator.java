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
 * JSON operations with access to a reusable backend, object conversion, and structural comparison.
 * Operators retain the backend supplied at construction. Creating a new wrapper with {@code with}
 * does not itself copy the backend or its custom collaborators.
 *
 * @param <J> the backend type, such as {@link JsonMapper} or {@link Gson}
 * @author allurx
 */
public interface JsonOperator<J> extends JsonOperation {

    /**
     * Returns the actual backend used by this operator, without copying it.
     *
     * @return the non-null JSON backend
     */
    J subject();

    /**
     * Creates a new operator around the supplied backend without copying it.
     * The supplier is invoked once; supplying the existing backend shares that instance.
     *
     * @param supplier a non-null supplier of a non-null backend
     * @return a new operator
     * @throws NullPointerException if the supplier or its result is null
     */
    JsonOperator<J> with(Supplier<J> supplier);

    /**
     * Creates a new operator around the transformed backend.
     * Use {@code with(mapper -> mapper.rebuild().enable(...).build())} for Jackson or
     * {@code with(gson -> gson.newBuilder().setPrettyPrinting().create())} for Gson.
     * The transformation runs once against the existing backend; returning it unchanged shares it.
     * Mutating a custom collaborator in the transformation can also affect the original operator.
     *
     * @param unaryOperator a non-null transformation returning a non-null backend
     * @return a new operator; the original operator keeps its backend
     * @throws NullPointerException if the transformation or its result is null
     */
    JsonOperator<J> with(UnaryOperator<J> unaryOperator);

    /**
     * Copies properties using the backend's in-memory conversion into a dynamically supplied type.
     * Use the class or type-token overload for a statically typed result.
     * This is a JSON mapping operation, not a field-by-field clone: configured serializers,
     * deserializers, and property rules determine the result. No existing target object is updated,
     * and preservation of object identity or cyclic references is not guaranteed.
     *
     * @param source the source object
     * @param type the non-null target type
     * @return the converted value, possibly null
     */
    Object copyProperties(Object source, Type type);

    /**
     * Converts a value to the specified class using the same mapping rules as
     * {@link #copyProperties(Object, Type)}.
     *
     * @param source the source object
     * @param type the non-null target class
     * @param <T> the target type
     * @return the converted value, possibly null
     */
    <T> T copyProperties(Object source, Class<T> type);

    /**
     * Converts a value to the type captured by the token, retaining generic arguments and using
     * the same mapping rules as {@link #copyProperties(Object, Type)}.
     *
     * @param source the source object
     * @param typeToken the non-null target type token
     * @param <T> the target type
     * @return the converted value, possibly null
     */
    <T> T copyProperties(Object source, TypeToken<T> typeToken);

    /**
     * Compares JSON values structurally, ignoring object property order and preserving array order.
     * Numeric equality follows the concrete operator's comparison rules.
     * Each visited input is parsed once, including a single supplied input. Comparison stops at
     * the first mismatch, so later inputs are not validated. Accepted syntax follows the backend;
     * this method does not provide backend-independent JSON validation.
     *
     * @param jsons the JSON strings to compare; must contain at least one element
     * @return whether all supplied JSON values are equivalent
     * @throws IllegalArgumentException if no JSON strings are supplied
     * @throws NullPointerException if the input array is null
     */
    boolean compare(String... jsons);

    /**
     * A shared operator using Jackson's native defaults, without Kit-specific configuration.
     * Derive local settings with {@link JacksonOperator#with(UnaryOperator)}.
     */
    JacksonOperator JACKSON_OPERATOR = new JacksonOperator(JsonMapper.builder().build());

    /**
     * A shared operator using Gson's native defaults, without Kit-specific configuration.
     * Derive local settings with {@link GsonOperator#with(UnaryOperator)}.
     */
    GsonOperator GSON_OPERATOR = new GsonOperator(new Gson());
}
