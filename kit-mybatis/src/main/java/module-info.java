/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * MyBatis handlers that bind declared Java types as JSON text through JDBC.
 * Public handler signatures expose MyBatis, JDBC, JSON operations, and Jackson subtype policies.
 * <p>The pinned MyBatis dependency uses an automatic module name; the suppression below acknowledges
 * the intentional transitive dependency on that automatic module.</p>
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
