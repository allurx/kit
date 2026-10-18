/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Chrome launch configuration and WebDriver session lifecycle management.
 * {@link io.allurx.kit.selenium.Chrome.ChromeBuilder} creates sessions according to
 * {@link io.allurx.kit.selenium.Mode}; each returned {@link io.allurx.kit.selenium.Chrome}
 * owns its resources and should be closed with try-with-resources.
 *
 * @author allurx
 */
package io.allurx.kit.selenium;
