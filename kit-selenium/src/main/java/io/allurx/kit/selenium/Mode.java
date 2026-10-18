/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
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
     * Kit launches a new Chrome process and connects ChromeDriver to that process's debugging port.
     * Existing browsers cannot be selected through this mode.
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

