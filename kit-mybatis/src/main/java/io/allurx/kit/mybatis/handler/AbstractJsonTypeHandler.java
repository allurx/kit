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
import io.allurx.kit.json.JsonOperation;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Base class for JSON type handlers, providing basic methods for serializing and deserializing objects.
 *
 * @param <T> The type of object returned by the mapper methods
 * @author allurx
 */
public abstract class AbstractJsonTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * JSON operator used for serialization and deserialization
     */
    private final JsonOperation jsonOperation;

    /**
     * The type of the object to be handled
     */
    private final TypeToken<T> type;

    /**
     * Constructor.
     *
     * @param jsonOperation The JSON serialization and deserialization operations
     * @param type         The {@link #type} of the object to be handled
     */
    protected AbstractJsonTypeHandler(JsonOperation jsonOperation, Class<T> type) {
        this(jsonOperation, TypeToken.of(Objects.requireNonNull(type, "type")));
    }

    /**
     * Creates a handler for a parameterized target type.
     *
     * @param jsonOperation The JSON serialization and deserialization operations
     * @param type The target type, including its generic arguments
     */
    protected AbstractJsonTypeHandler(JsonOperation jsonOperation, TypeToken<T> type) {
        this.jsonOperation = Objects.requireNonNull(jsonOperation, "jsonOperation");
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * Registers this instance using the constructor's target type instead of MyBatis's generic-type inference.
     * Use this method instead of {@code registry.register(handler)} for programmatic registration.
     * Parameterized targets register under their raw class; generic arguments remain available for JSON operations.
     *
     * @param registry the registry to receive this handler
     */
    public final void registerTo(TypeHandlerRegistry registry) {
        registry.register(type.getRawClass(), this);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        // Use the read type when writing so enabled polymorphic handling retains root and generic element type ids.
        ps.setString(i, jsonOperation.toJsonString(parameter, type.getType()));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return read(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return read(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return read(cs.getString(columnIndex));
    }

    private T read(String json) {
        return json == null ? null : jsonOperation.fromJsonString(json, type);
    }
}
