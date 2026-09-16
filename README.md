# Kit

Java 25+ utilities with explicit JPMS modules.

| Artifact | Purpose | Versions |
| --- | --- | --- |
| `kit-base` | Conditional chains, polling, functional helpers, and generic type tokens; no runtime dependencies | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-base) |
| `kit-json` | Jackson and Gson serialization, conversion, and structural comparison | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-json) |
| `kit-mybatis` | MyBatis JSON type handlers with declared and polymorphic types | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-mybatis) |
| `kit-selenium` | Chrome startup and WebDriver lifecycle management | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-selenium) |

## Installation

Open an artifact's Maven Central link above to find the latest published version
and copy the Maven dependency declaration for the version you want to use.

## Usage notes

- **JSON:** Shared operators use backend defaults. Use `Class<T>` or `TypeToken<T>` for typed results;
  `Type`-based deserialization and conversion return `Object`. Customize with `with(mapper -> mapper.rebuild()...build())`
  or `with(gson -> gson.newBuilder()...create())` and keep the returned operator.
  Named modules should open model packages to `tools.jackson.databind` or `com.google.gson` as needed.
  See [JsonOperator](kit-json/src/main/java/io/allurx/kit/json/JsonOperator.java).
- **MyBatis:** `SimpleJsonTypeHandler` uses Jackson defaults; `GenericJsonTypeHandler` adds polymorphic
  metadata and requires an explicit `PolymorphicTypeValidator`. Register instances with
  `handler.registerTo(configuration.getTypeHandlerRegistry())`. Parameterized targets share their raw-class
  registry key, so different element types need explicit property mappings.
  See [handler contracts](kit-mybatis/src/main/java/io/allurx/kit/mybatis/handler/AbstractJsonTypeHandler.java).
- **Chrome:** Close each instance with try-with-resources. ATTACH launches a Kit-owned browser;
  use a dedicated user data directory. See [Chrome](kit-selenium/src/main/java/io/allurx/kit/selenium/Chrome.java).

## Building

Use Maven 3.9.x and JDK 25+ to run `mvn verify` from the repository root.
Run `mvn install` when other local projects need the updated Kit artifacts.
The real Chrome startup test is opt-in; read [kit-selenium/AGENTS.md](kit-selenium/AGENTS.md)
before browser tests. See [CI and releases](docs/ci-cd.md) for Kit's verification and
release conventions and links to the shared workflow documentation.

## License

[Apache License 2.0](LICENSE.txt)
