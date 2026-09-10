# Kit

Java 25+ utilities with explicit JPMS modules.

| Artifact | Purpose |
| --- | --- |
| `kit-base` | Conditional chains, polling, functional helpers, and generic type tokens; no runtime dependencies |
| `kit-json` | Jackson and Gson serialization, conversion, and structural comparison |
| `kit-mybatis` | MyBatis JSON type handlers with declared and polymorphic types |
| `kit-selenium` | Chrome startup and WebDriver lifecycle management |

## Installation

Choose an artifact and set `kit.version` to the release version you use:

```xml
<dependency>
    <groupId>io.allurx.kit</groupId>
    <artifactId>kit-base</artifactId>
    <version>${kit.version}</version>
</dependency>
```

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

Build with Maven running on JDK 25+. Core verification: `mvn -pl kit-mybatis -am verify`.
For browser tests, read [kit-selenium/AGENTS.md](kit-selenium/AGENTS.md).

## License

[Apache License 2.0](LICENSE.txt)
