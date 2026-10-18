/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * JSON operator tests with reflective access to model fixtures for JUnit and the JSON backends.
 *
 * @author allurx
 */
module io.allurx.kit.json.test {
    requires org.junit.jupiter.api;
    requires io.allurx.kit.base;
    requires io.allurx.kit.json;
    exports io.allurx.kit.json.test;
    opens io.allurx.kit.json.test;
}