/*
 * Copyright 2024 allurx
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Tests for base utilities and separate JMH benchmarks for conditional flows.
 *
 * @author allurx
 */
module io.allurx.kit.base.test {
    requires io.allurx.kit.base;
    requires org.junit.jupiter.api;
    requires jmh.core;
    requires java.net.http;
    exports io.allurx.kit.base.test;
    exports io.allurx.kit.base.benchmark;
    exports io.allurx.kit.base.test.poller;
    opens io.allurx.kit.base.test;
    opens io.allurx.kit.base.benchmark;
    opens io.allurx.kit.base.test.poller;
}