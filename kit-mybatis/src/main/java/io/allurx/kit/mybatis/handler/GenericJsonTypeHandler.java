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
 * Preserves polymorphic values using an explicit application-defined subtype policy.
 * The validator must allow each permitted runtime type, including container implementations
 * when they require type information. Use the narrowest policy suitable for the stored models.
 * Each handler owns an immutable mapper derived from Jackson's default configuration.
 * Automatic type information uses Jackson's default wrapper-array representation.
 *
 * <p>Register an instance with MyBatis programmatically, or define a subclass whose
 * {@link Class} constructor supplies the application's validator. No unrestricted
 * default validator is provided.</p>
 *
 * @param <T> The type of object returned by the mapper methods
 * @author allurx
 */
public class GenericJsonTypeHandler<T> extends AbstractJsonTypeHandler<T> {

    /**
     * Creates a handler for a declared target class and its permitted runtime subtypes.
     *
     * @param type The declared target class
     * @param validator The explicit policy for allowed polymorphic subtypes
     */
    public GenericJsonTypeHandler(Class<T> type, PolymorphicTypeValidator validator) {
        super(operator(validator), type);
    }

    /**
     * Creates a handler for a parameterized target type and its permitted runtime subtypes.
     *
     * @param type The target type, including its generic arguments
     * @param validator The explicit policy for allowed polymorphic subtypes
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
