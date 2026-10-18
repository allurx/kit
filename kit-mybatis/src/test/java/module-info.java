/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Handler registration tests; the production module supplies the public API's transitive dependencies.
 *
 * @author allurx
 */
module io.allurx.kit.mybatis.test {
    requires io.allurx.kit.mybatis;
    requires org.junit.jupiter.api;
    opens io.allurx.kit.mybatis.test;
}
