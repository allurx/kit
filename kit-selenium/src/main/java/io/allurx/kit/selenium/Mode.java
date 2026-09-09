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
 * {@link WebDriver} communication modes with the browser.
 * <p>
 * This enum defines two modes of interaction:
 * </p>
 * <ul>
 *     <li>{@link #ATTACH} - Attaches ChromeDriver to a Chrome debugging address and controls that browser through W3C WebDriver.</li>
 *     <li>{@link #HOSTED} - Uses W3C WebDriver with Chrome's lifecycle managed by the driver.</li>
 * </ul>
 *
 * @author allurx
 */
public enum Mode {

    /**
     * Attaches ChromeDriver to a Chrome debugging address and controls that browser through W3C WebDriver.
     */
    ATTACH,

    /**
     * Communicates with Chrome using W3C WebDriver.
     * In this mode, the lifecycle of Chrome is entirely controlled by {@link WebDriver}.
     */
    HOSTED
}

