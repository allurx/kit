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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.By;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies Chrome startup and local page interaction through the public API
 * using a temporary profile.
 * Enable with {@code -Dkit.selenium.chromePath=/path/to/chrome}.
 *
 * @author allurx
 */
@EnabledIfSystemProperty(named = "kit.selenium.chromePath", matches = ".+")
public class ChromeStartupTest {

    private List<ProcessHandle> ownedProcesses = List.of();

    @Test
    @Timeout(60)
    void startsChrome(@TempDir Path profile) {
        try (var chrome = Chrome.builder()
                .mode(Mode.ATTACH)
                .chromePath(System.getProperty("kit.selenium.chromePath"))
                .addArgs("--headless", "--user-data-dir=" + profile)
                .build()) {
            try {
                var driver = chrome.webDriver();
                var html = """
                        <!doctype html>
                        <meta charset="utf-8">
                        <title>Kit Chrome test</title>
                        <label>Name <input id="name"></label>
                        <button id="greet"
                                onclick="document.getElementById('message').textContent =
                                         'Hello, ' + document.getElementById('name').value">Greet</button>
                        <output id="message"></output>
                        """;
                var page = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
                driver.get("data:text/html;base64," + page);
                assertEquals("Kit Chrome test", driver.getTitle());

                driver.findElement(By.id("name")).sendKeys("Kit");
                driver.findElement(By.id("greet")).click();
                assertEquals("Hello, Kit", driver.findElement(By.id("message")).getText());
            } finally {
                // This isolated test JVM owns the browser and driver processes created by this scenario.
                ownedProcesses = ProcessHandle.current().descendants().toList();
            }
        }
    }

    @AfterEach
    void awaitOwnedProcesses() throws Exception {
        // Chrome.close requests exit asynchronously; wait before JUnit deletes the temporary profile.
        var exits = ownedProcesses.stream().map(ProcessHandle::onExit).toArray(CompletableFuture<?>[]::new);
        CompletableFuture.allOf(exits).get(5, TimeUnit.SECONDS);
    }
}
