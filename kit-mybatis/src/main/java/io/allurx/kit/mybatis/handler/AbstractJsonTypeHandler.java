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
 * Maps a declared Java type to JSON text using JDBC {@code setString} and {@code getString}.
 * The same declared type is used for writing and reading, including generic arguments.
 * The database column and driver must accept string binding; database-specific JSON types
 * may require explicit SQL casts or a different handler.
 *
 * <p>SQL {@code NULL} is returned as Java null without invoking JSON deserialization.
 * Non-null column contents, including the JSON literal {@code null}, are passed to the backend.
 * Null parameters are handled by {@link BaseTypeHandler#setParameter} using the supplied JDBC type.
 * Direct calls to the methods declared here propagate JDBC and JSON failures; inherited
 * {@code setParameter} and {@code getResult} calls add MyBatis parameter or result context.
 *
 * @param <T> the declared Java value type
 * @author allurx
 */
public abstract class AbstractJsonTypeHandler<T> extends BaseTypeHandler<T> {

    /**
     * Serialization and deserialization configuration for the stored JSON format.
     */
    private final JsonOperation jsonOperation;

    /**
     * Declared read and write type, retaining generic arguments independently of registry lookup.
     */
    private final TypeToken<T> type;

    /**
     * Creates a handler for a concrete target class.
     *
     * @param jsonOperation the non-null JSON operations
     * @param type the non-null target class
     * @throws NullPointerException if either argument is null
     */
    protected AbstractJsonTypeHandler(JsonOperation jsonOperation, Class<T> type) {
        this(jsonOperation, TypeToken.of(Objects.requireNonNull(type, "type")));
    }

    /**
     * Creates a handler for a parameterized target type.
     *
     * @param jsonOperation the non-null JSON operations
     * @param type the non-null target type, including its generic arguments
     * @throws NullPointerException if either argument is null
     */
    protected AbstractJsonTypeHandler(JsonOperation jsonOperation, TypeToken<T> type) {
        this.jsonOperation = Objects.requireNonNull(jsonOperation, "jsonOperation");
        this.type = Objects.requireNonNull(type, "type");
    }

    /**
     * Registers this instance using the constructor's target type instead of MyBatis's generic-type inference.
     * Use this method instead of {@code registry.register(handler)} for programmatic registration.
     * Parameterized targets register under their raw class; generic arguments remain available for JSON operations.
     * Consequently, handlers for {@code List<Person>} and {@code List<Address>} share the same
     * Java-type key and cannot be selected by element type. Use explicit mappings or separate
     * registries when the same raw type requires different configurations.
     *
     * @param registry the non-null registry to receive this handler
     * @throws NullPointerException if the registry is null
     */
    public final void registerTo(TypeHandlerRegistry registry) {
        registry.register(type.getRawClass(), this);
    }

    /**
     * Serializes a non-null parameter with the declared type and binds the JSON text.
     *
     * @param ps the target statement
     * @param i the one-based parameter index
     * @param parameter the non-null Java value
     * @param jdbcType the MyBatis JDBC type; string binding does not use it
     * @throws SQLException if JDBC string binding fails
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        // Use the read type when writing so enabled polymorphic handling retains root and generic element type ids.
        ps.setString(i, jsonOperation.toJsonString(parameter, type.getType()));
    }

    /**
     * Reads a named column as JSON using the declared target type.
     *
     * @param rs the result set positioned on a row
     * @param columnName the column label
     * @return the deserialized value, or null for SQL {@code NULL}
     * @throws SQLException if JDBC string retrieval fails
     */
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return read(rs.getString(columnName));
    }

    /**
     * Reads an indexed column as JSON using the declared target type.
     *
     * @param rs the result set positioned on a row
     * @param columnIndex the one-based column index
     * @return the deserialized value, or null for SQL {@code NULL}
     * @throws SQLException if JDBC string retrieval fails
     */
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return read(rs.getString(columnIndex));
    }

    /**
     * Reads an output parameter as JSON using the declared target type.
     *
     * @param cs the executed callable statement
     * @param columnIndex the one-based output parameter index
     * @return the deserialized value, or null for SQL {@code NULL}
     * @throws SQLException if JDBC string retrieval fails
     */
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return read(cs.getString(columnIndex));
    }

    private T read(String json) {
        return json == null ? null : jsonOperation.fromJsonString(json, type);
    }
}
