/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */

package io.allurx.kit.mybatis.handler;

import io.allurx.kit.base.reflection.TypeToken;
import io.allurx.kit.json.JsonOperator;

/**
 * Serializes and deserializes values using the shared Jackson operator's default configuration.
 * Both operations use the declared target type; this handler does not enable default typing.
 * Model annotations, including explicit polymorphic type information, still participate in Jackson mapping.
 * Use {@link GenericJsonTypeHandler} when automatic type metadata with an explicit subtype policy is needed.
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
     * Creates a handler for a target type captured by a token, retaining any generic arguments.
     * The token retains generic arguments for JSON conversion; registry lookup still uses its raw class.
     *
     * @param type the non-null target type, including its generic arguments
     * @throws NullPointerException if the target type is null
     */
    public SimpleJsonTypeHandler(TypeToken<T> type) {
        super(JsonOperator.JACKSON_OPERATOR, type);
    }
}

