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

package red.zyc.kit.mybatis.handler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import red.zyc.kit.json.JacksonOperator;
import red.zyc.kit.json.JsonOperator;


/**
 * Preserves type information of objects in JSON strings using a configured {@link ObjectMapper} to ensure accurate restoration of the original object during deserialization.
 * Supports serialization and deserialization of generic objects.
 *
 * @param <T> The type of object returned by the mapper methods
 * @author allurx
 * @see GenericJsonTypeHandler#JACKSON_OPERATOR
 */
public class GenericJsonTypeHandler<T> extends AbstractJsonTypeHandler<T, ObjectMapper> {

    /**
     * JSON operator with default typing activated to preserve type information.
     */
    private static final JacksonOperator JACKSON_OPERATOR = JsonOperator.JACKSON_OPERATOR.with(ObjectMapper::copy)
            .configure(objectMapper -> objectMapper.activateDefaultTyping(
                    objectMapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY
            ));

    /**
     * Constructor.
     *
     * @param clazz The type of object returned
     */
    public GenericJsonTypeHandler(Class<T> clazz) {
        super(JACKSON_OPERATOR, clazz);
    }
}
