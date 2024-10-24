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
package io.allurx.kit.selenium.test;

import io.allurx.kit.selenium.Chrome;
import io.allurx.kit.selenium.Mode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author allurx
 */
public class TelegramTest {

    @Test
    void testTelegram() {
        try (var chrome = Chrome.builder()
                .mode(Mode.ATTACH)
                .addArgs("--user-data-dir=D:\\chrome-user-data\\1-8502950634")
                .build()) {
            var webDriver = chrome.webDriver();
            webDriver.get("https://web.telegram.org/a/");
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));
        }
    }
}
