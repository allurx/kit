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
    // Selenium's HTTP response handling uses Guava at runtime despite its static module requirement.
    requires com.google.common;
    requires io.allurx.kit.base;
}
