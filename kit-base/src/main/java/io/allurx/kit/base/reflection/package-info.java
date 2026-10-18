/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Captures declared generic types and runtime type-use annotations without resolving call-site variables.
 * Raw-class access erases generic arguments; unchecked casts require caller-verified compatibility.
 *
 * @author allurx
 */
package io.allurx.kit.base.reflection;
