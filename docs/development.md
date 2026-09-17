# Development

Use Maven 3.9.x and JDK 25+ from the repository root. Confirm the active JDK with
`mvn -version`; in IDEA, set **Maven → Runner → JRE** accordingly.

## Local verification

Use `-am` to build selected modules' reactor dependencies. For base, JSON and MyBatis:

```sh
mvn -pl kit-mybatis -am verify
```

Verify the full reactor with sources and Javadoc, without signing or publishing:

```sh
mvn -Prelease "-Dgpg.skip=true" clean verify
```

The `release` profile comes from [maven-parent](https://github.com/allurx/maven-parent).

### Notes

- MyBatis tests do not verify compatibility with an actual database.
- JMH runs separately from JUnit; passing tests do not establish performance.

## Browser testing

The [Chrome startup test](../kit-selenium/src/test/java/io/allurx/kit/selenium/test/ChromeStartupTest.java)
uses a real headless browser in `ATTACH` mode with a temporary profile.
Replace the example path with an installed Chrome executable:

```sh
mvn -pl kit-selenium -am "-Dkit.selenium.chromePath=/absolute/path/to/chrome" verify
```

### Notes

- Without `kit.selenium.chromePath`, the test skips; browser startup remains unverified.
- Manual scripts in `kit-selenium/scripts/windows` are outside the tests and published JAR.
  Launch scripts use persistent profiles and external services.
- `kill-chrome-and-driver.bat` terminates all Chrome and ChromeDriver processes by name.
  Do not use it for test cleanup; target only processes created by the test.

## Version changes

Update the root Kit version and all four child POMs' Kit parent versions together.
The external `maven-parent` version is independent.
