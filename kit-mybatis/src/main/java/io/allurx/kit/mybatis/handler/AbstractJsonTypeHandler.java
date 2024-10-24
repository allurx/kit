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

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import io.allurx.kit.json.JsonOperator;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Base class for JSON type handlers, providing basic methods for serializing and deserializing objects.
 *
 * @param <T> The type of object returned by the mapper methods
 * @param <J> The type of the JSON operator
 * @author allurx
 */
public abstract class AbstractJsonTypeHandler<T, J> extends BaseTypeHandler<T> {

    /**
     * JSON operator used for serialization and deserialization
     */
    private final JsonOperator<J> jsonOperator;

    /**
     * The type of the object to be handled
     */
    private final Type type;

    /**
     * Constructor.
     *
     * @param jsonOperator The {@link JsonOperator} used for JSON operations
     * @param type         The {@link #type} of the object to be handled
     */
    public AbstractJsonTypeHandler(JsonOperator<J> jsonOperator, Type type) {
        this.jsonOperator = jsonOperator;
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, jsonOperator.toJsonString(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Optional.ofNullable(rs.getString(columnName))
                .map(s -> jsonOperator.<T>fromJsonString(s, type))
                .orElse(null);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Optional.ofNullable(rs.getString(columnIndex))
                .map(s -> jsonOperator.<T>fromJsonString(s, type))
                .orElse(null);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Optional.ofNullable(cs.getString(columnIndex))
                .map(s -> jsonOperator.<T>fromJsonString(s, type))
                .orElse(null);
    }

}
