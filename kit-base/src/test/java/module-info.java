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
 * base test module
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