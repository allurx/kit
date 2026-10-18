/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Chrome launch and session lifecycle utilities built on Selenium.
 * Selenium's API is readable by consumers because {@code Chrome.webDriver()} exposes its types;
 * ChromeDriver and logging remain implementation dependencies. Applications supply their own SLF4J provider.
 *
 * @author allurx
 */
module io.allurx.kit.selenium {
    exports io.allurx.kit.selenium;
    requires org.slf4j;
    requires transitive org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver;
    // Resolve Guava for Selenium's HTTP response codec; Selenium declares it only as a static requirement.
    requires com.google.common;
    requires io.allurx.kit.base;
}
