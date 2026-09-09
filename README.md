# Kit

Kit, developed with **JDK 25**, is a lightweight Java utility library designed to enhance productivity by streamlining development,
simplifying common tasks, improving code readability, and fully leveraging modern Java features with an easy-to-use API for diverse development needs.

## Installation

Kit requires Java 25 or later. Building from source also requires Maven to run on JDK 25 or later.

`kit-json` and `kit-mybatis` depend on Jackson **3.2.2**.

To use Kit in your Maven project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.allurx.kit</groupId>
    <artifactId>kit-${module}</artifactId>
    <version>${latest version}</version>
</dependency>
```

## JSON operations

`JsonOperator.JACKSON_OPERATOR` and `JsonOperator.GSON_OPERATOR` use their backends' native defaults.
Use `Class<T>` or `TypeToken<T>` for statically typed results; overloads accepting a dynamic `Type` return `Object`.
Create an independently configured operator with `with(mapper -> mapper.rebuild()...build())` for Jackson
or `with(gson -> gson.newBuilder()...create())` for Gson, and retain the returned operator.
Named-module applications should open model packages to the backend they use: `tools.jackson.databind` or `com.google.gson`.

MyBatis JSON handlers serialize and deserialize using the declared target type.
`SimpleJsonTypeHandler` uses Jackson's default configuration without adding automatic type metadata.
`GenericJsonTypeHandler` retains polymorphic type information in wrapper arrays and requires an explicit
`PolymorphicTypeValidator`; register a configured instance or a subclass that supplies the validator.

## License

This project is licensed under the Apache License 2.0. You may obtain a copy of the License at:

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
