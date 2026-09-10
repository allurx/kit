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
/**
 * MyBatis handlers that bind declared Java types as JSON text through JDBC.
 * Public handler signatures expose MyBatis, JDBC, JSON operations, and Jackson subtype policies.
 * <p>MyBatis declares a stable automatic module name but has no explicit module descriptor.</p>
 *
 * @author allurx
 */
@SuppressWarnings("requires-transitive-automatic")
module io.allurx.kit.mybatis {
    requires transitive org.mybatis;
    requires transitive java.sql;
    requires transitive io.allurx.kit.base;
    requires transitive io.allurx.kit.json;
    requires transitive tools.jackson.databind;
    exports io.allurx.kit.mybatis.handler;
}
