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
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies Chrome startup through the public API using a temporary profile.
 * Enable with {@code -Dkit.selenium.chromePath=/path/to/chrome}.
 *
 * @author allurx
 */
@EnabledIfSystemProperty(named = "kit.selenium.chromePath", matches = ".+")
public class ChromeStartupTest {

    @Test
    @Timeout(60)
    void startsChrome(@TempDir Path profile) {
        try (var chrome = Chrome.builder()
                .mode(Mode.ATTACH)
                .chromePath(System.getProperty("kit.selenium.chromePath"))
                .addArgs("--headless", "--user-data-dir=" + profile)
                .build()) {
            assertFalse(chrome.webDriver().getWindowHandles().isEmpty());
        }
    }
}
