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
import io.allurx.kit.json.JacksonOperator;
import io.allurx.kit.json.JsonOperator;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.util.Objects;

/**
 * Stores polymorphic JSON using Jackson's {@link DefaultTyping#NON_FINAL} and an explicit
 * application-defined subtype policy.
 * The validator must allow each permitted runtime type, including container implementations
 * when they require type information. Use the narrowest policy suitable for the stored models.
 * Each handler uses a rebuilt mapper, leaving the shared Jackson operator unchanged.
 * Automatic type information uses Jackson's default wrapper-array representation and Java class names.
 * Renaming classes, changing declared types, or changing the subtype policy can make existing
 * database values unreadable; the handler does not migrate stored JSON.
 *
 * <p>The validator controls polymorphic subtype resolution when reading stored JSON; it is not
 * application-level validation of the resulting values. Treat database content according to its
 * source and restrict allowed types accordingly. A policy that accepts every subtype removes
 * this restriction; no unrestricted default validator is provided.
 *
 * <p>Register an instance with {@link #registerTo(org.apache.ibatis.type.TypeHandlerRegistry)},
 * or define a subclass whose {@link Class} constructor supplies the application's validator.
 * The two-argument constructors cannot be used directly by MyBatis's class-only instantiation.
 *
 * @param <T> the declared Java value type
 * @author allurx
 */
public class GenericJsonTypeHandler<T> extends AbstractJsonTypeHandler<T> {

    /**
     * Creates a handler for a declared target class and its permitted runtime subtypes.
     *
     * @param type the non-null declared target class
     * @param validator the non-null policy for allowed polymorphic subtypes
     * @throws NullPointerException if either argument is null
     */
    public GenericJsonTypeHandler(Class<T> type, PolymorphicTypeValidator validator) {
        super(operator(validator), type);
    }

    /**
     * Creates a handler for a parameterized target type and its permitted runtime subtypes.
     *
     * @param type the non-null target type, including its generic arguments
     * @param validator the non-null policy for allowed polymorphic subtypes
     * @throws NullPointerException if either argument is null
     */
    public GenericJsonTypeHandler(TypeToken<T> type, PolymorphicTypeValidator validator) {
        super(operator(validator), type);
    }

    private static JacksonOperator operator(PolymorphicTypeValidator validator) {
        Objects.requireNonNull(validator, "validator");
        return JsonOperator.JACKSON_OPERATOR.with(mapper -> mapper.rebuild()
                .activateDefaultTyping(validator, DefaultTyping.NON_FINAL)
                .build());
    }
}
