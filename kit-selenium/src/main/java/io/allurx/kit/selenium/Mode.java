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
package io.allurx.kit.selenium;

import org.openqa.selenium.WebDriver;

/**
 * Defines who starts Chrome before it is controlled via {@link WebDriver}.
 *
 * @author allurx
 */
public enum Mode {

    /**
     * Kit starts and owns the Chrome process; ChromeDriver connects to its debugging port.
     * This mode launches a new process rather than attaching to an arbitrary existing browser.
     * Supply a dedicated user data directory through {@link Chrome.ChromeBuilder#addArgs(String...)}.
     *
     * @see Chrome.ChromeBuilder#build()
     */
    ATTACH,

    /**
     * ChromeDriver starts Chrome and manages its lifecycle.
     */
    HOSTED
}

