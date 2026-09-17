# Kit

Java 25+ utilities with explicit JPMS modules.

| Artifact | Purpose | Versions |
| --- | --- | --- |
| `kit-base` | Conditional chains, polling, functional helpers, and generic type tokens; no runtime dependencies | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-base) |
| `kit-json` | Jackson and Gson serialization, conversion, and structural comparison | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-json) |
| `kit-mybatis` | MyBatis JSON type handlers with declared and polymorphic types | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-mybatis) |
| `kit-selenium` | Chrome startup and WebDriver lifecycle management | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-selenium) |

## Installation

Use the Maven Central links above to find the latest release and copy your chosen
version's dependency declaration.

## Usage notes

- **JSON:** Shared operators use backend defaults. Use `Class<T>` or `TypeToken<T>` for typed results;
  `Type`-based deserialization and conversion return `Object`. Customize with `with(mapper -> mapper.rebuild()...build())`
  or `with(gson -> gson.newBuilder()...create())` and use the returned operator.
  Named modules should open model packages to `tools.jackson.databind` or `com.google.gson` as needed.
  See [JsonOperator](kit-json/src/main/java/io/allurx/kit/json/JsonOperator.java).
- **MyBatis:** `SimpleJsonTypeHandler` uses Jackson defaults; `GenericJsonTypeHandler` enables Jackson
  `NON_FINAL` default typing and requires an explicit `PolymorphicTypeValidator`. Register handlers with
  `handler.registerTo(configuration.getTypeHandlerRegistry())`. Parameterized types share a raw-class
  registry key; map properties explicitly when element types differ.
  See [handler contracts](kit-mybatis/src/main/java/io/allurx/kit/mybatis/handler/AbstractJsonTypeHandler.java).
- **Chrome:** Close instances with try-with-resources. `ATTACH` launches a Kit-owned browser;
  use a dedicated user data directory. See [Chrome](kit-selenium/src/main/java/io/allurx/kit/selenium/Chrome.java).

### Notes

MyBatis handlers use JDBC `setString` / `getString`; native JSON columns may require
SQL casts. Changing model class names, declared types or subtype policies can make
stored polymorphic JSON unreadable; handlers do not migrate stored values.
See [GenericJsonTypeHandler](kit-mybatis/src/main/java/io/allurx/kit/mybatis/handler/GenericJsonTypeHandler.java)
for type-metadata and subtype-policy constraints.

## Building

From the repository root, use Maven 3.9.x and JDK 25+ to run `mvn verify`;
use `mvn install` to make artifacts available to other local projects.
See [development](docs/development.md) for focused builds, sources/Javadoc checks
and the opt-in Chrome startup test.

CI and releases use [allurx-build](https://github.com/allurx/allurx-build).

## License

[Apache License 2.0](LICENSE.txt)
