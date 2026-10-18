/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Opt-in Chrome startup test using the public API.
 *
 * @author allurx
 */
module io.allurx.kit.selenium.test {
    requires io.allurx.kit.selenium;
    requires org.junit.jupiter.api;
    exports io.allurx.kit.selenium.test;
    opens io.allurx.kit.selenium.test;
}
