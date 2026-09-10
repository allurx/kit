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
package io.allurx.kit.mybatis.test;

import io.allurx.kit.base.reflection.TypeToken;
import io.allurx.kit.mybatis.handler.GenericJsonTypeHandler;
import io.allurx.kit.mybatis.handler.SimpleJsonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.TypeReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies registration by constructor-supplied target types and retention of generic element types.
 * JDBC results are proxies supplying JSON text; these tests do not exercise a database or driver.
 *
 * @author allurx
 */
class JsonTypeHandlerRegistrationTest {

    record Person(String name) {}

    record Address(String city) {}

    @Test
    void simpleHandlersRegisterUnderTheirOwnTargetClasses() {
        var registry = new TypeHandlerRegistry();
        var people = new SimpleJsonTypeHandler<>(Person.class);
        var addresses = new SimpleJsonTypeHandler<>(Address.class);

        people.registerTo(registry);
        addresses.registerTo(registry);

        assertSame(people, registry.getTypeHandler(Person.class));
        assertSame(addresses, registry.getTypeHandler(Address.class, JdbcType.VARCHAR));
    }

    @Test
    void genericHandlersRegisterUnderTheirOwnTargetClasses() {
        var registry = new TypeHandlerRegistry();
        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Person.class).allowIfSubType(Address.class).build();
        var people = new GenericJsonTypeHandler<>(Person.class, validator);
        var addresses = new GenericJsonTypeHandler<>(Address.class, validator);

        people.registerTo(registry);
        addresses.registerTo(registry);

        assertSame(people, registry.getTypeHandler(Person.class));
        assertSame(addresses, registry.getTypeHandler(Address.class, JdbcType.VARCHAR));
    }

    @Test
    void simpleParameterizedHandlerPreservesElementTypesAfterRegistration() throws SQLException {
        var registry = new TypeHandlerRegistry();
        var handler = new SimpleJsonTypeHandler<>(new TypeToken<List<Person>>() {});

        handler.registerTo(registry);

        assertSame(handler, registry.getTypeHandler(List.class));
        var registered = registry.getTypeHandler(new TypeReference<List<Person>>() {});
        assertSame(handler, registered);
        assertEquals(List.of(new Person("Alice")),
                registered.getResult(resultSet("[{\"name\":\"Alice\"}]"), "value"));
    }

    @Test
    void genericParameterizedHandlerPreservesElementTypesAfterRegistration() throws SQLException {
        var registry = new TypeHandlerRegistry();
        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(ArrayList.class).allowIfSubType(Person.class).build();
        var handler = new GenericJsonTypeHandler<>(new TypeToken<List<Person>>() {}, validator);

        handler.registerTo(registry);

        assertSame(handler, registry.getTypeHandler(List.class));
        var registered = registry.getTypeHandler(new TypeReference<List<Person>>() {});
        assertSame(handler, registered);
        assertEquals(List.of(new Person("Alice")), registered.getResult(
                resultSet("[\"java.util.ArrayList\",[{\"name\":\"Alice\"}]]"), "value"));
    }

    private static ResultSet resultSet(String json) {
        return (ResultSet) Proxy.newProxyInstance(JsonTypeHandlerRegistrationTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getString")) {
                        return json;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
