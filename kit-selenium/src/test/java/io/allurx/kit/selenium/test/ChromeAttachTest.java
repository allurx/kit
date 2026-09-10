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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchSessionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies ATTACH mode with a real headless Chrome and an isolated profile.
 * Enable with {@code -Dkit.selenium.chromePath=/path/to/chrome}.
 *
 * @author allurx
 */
@EnabledIfSystemProperty(named = "kit.selenium.chromePath", matches = ".+")
public class ChromeAttachTest {

    @Test
    @Timeout(60)
    void attachesControlsPageAndClosesSession(@TempDir Path directory) throws IOException {
        Path page = Files.writeString(directory.resolve("page.html"), """
                <!doctype html>
                <title>Kit ATTACH</title>
                <button id="action" onclick="this.textContent='Clicked'">Click me</button>
                """);
        var chrome = Chrome.builder()
                .mode(Mode.ATTACH)
                .chromePath(System.getProperty("kit.selenium.chromePath"))
                .addArgs("--headless", "--user-data-dir=" + directory.resolve("profile"))
                .build();
        var webDriver = chrome.webDriver();
        try (chrome) {
            webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            webDriver.get(page.toUri().toString());
            assertEquals("Kit ATTACH", webDriver.getTitle());

            var button = webDriver.findElement(By.id("action"));
            button.click();
            assertEquals("Clicked", button.getText());
        }
        assertThrows(NoSuchSessionException.class, webDriver::getTitle);
    }
}
