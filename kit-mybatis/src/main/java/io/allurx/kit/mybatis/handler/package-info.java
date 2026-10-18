/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * MyBatis handlers for storing declared Java types as JSON text.
 * {@link io.allurx.kit.mybatis.handler.SimpleJsonTypeHandler} uses default Jackson mapping;
 * {@link io.allurx.kit.mybatis.handler.GenericJsonTypeHandler} adds polymorphic type information
 * under an explicit subtype policy. Their shared JDBC and registration contracts are defined by
 * {@link io.allurx.kit.mybatis.handler.AbstractJsonTypeHandler}.
 *
 * @author allurx
 */
package io.allurx.kit.mybatis.handler;
