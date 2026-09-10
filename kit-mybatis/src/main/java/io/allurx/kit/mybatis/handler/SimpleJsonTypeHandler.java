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

package io.allurx.kit.mybatis.handler;

import io.allurx.kit.base.reflection.TypeToken;
import io.allurx.kit.json.JsonOperator;

/**
 * Serializes and deserializes values using the shared Jackson operator's default configuration.
 * Both operations use the declared target type; this handler does not enable default typing.
 * Model annotations still participate in Jackson mapping.
 * Use {@link GenericJsonTypeHandler} with an explicit subtype policy to preserve runtime subtypes.
 * The {@link Class} constructor supports MyBatis type-handler registration;
 * a {@link TypeToken} can retain generic arguments for programmatic registration or a dedicated subclass.
 * Register configured instances with {@link #registerTo(org.apache.ibatis.type.TypeHandlerRegistry)}.
 *
 * @param <T> the declared Java value type
 * @author allurx
 */
public class SimpleJsonTypeHandler<T> extends AbstractJsonTypeHandler<T> {

    /**
     * Creates a handler for a concrete target class, including MyBatis constructor-based registration.
     *
     * @param clazz the non-null target class
     * @throws NullPointerException if the target class is null
     */
    public SimpleJsonTypeHandler(Class<T> clazz) {
        super(JsonOperator.JACKSON_OPERATOR, clazz);
    }

    /**
     * Creates a handler for a parameterized target type.
     * The token retains generic arguments for JSON conversion; registry lookup still uses its raw class.
     *
     * @param type the non-null target type, including its generic arguments
     * @throws NullPointerException if the target type is null
     */
    public SimpleJsonTypeHandler(TypeToken<T> type) {
        super(JsonOperator.JACKSON_OPERATOR, type);
    }
}

