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
 * Serializes and deserializes values using Jackson's default JSON configuration.
 * Both operations use the declared target type; no automatic polymorphic type information is added.
 * Use {@link GenericJsonTypeHandler} with an explicit subtype policy to preserve runtime subtypes.
 * The {@link Class} constructor supports MyBatis type-handler registration;
 * a {@link TypeToken} can retain generic arguments for programmatic registration or a dedicated subclass.
 *
 * @param <T> The type of object returned by the mapper methods
 * @author allurx
 */
public class SimpleJsonTypeHandler<T> extends AbstractJsonTypeHandler<T> {

    /**
     * Constructor.
     *
     * @param clazz The type of object returned
     */
    public SimpleJsonTypeHandler(Class<T> clazz) {
        super(JsonOperator.JACKSON_OPERATOR, clazz);
    }

    /**
     * Creates a handler for a parameterized target type.
     *
     * @param type The target type, including its generic arguments
     */
    public SimpleJsonTypeHandler(TypeToken<T> type) {
        super(JsonOperator.JACKSON_OPERATOR, type);
    }
}

