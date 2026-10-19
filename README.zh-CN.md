# Kit

[English](README.md) | 简体中文

面向 Java 25 及以上版本的工具库，各组件提供独立的 JPMS 模块。

| 制品 | 用途 | 版本 |
| --- | --- | --- |
| `kit-base` | 条件链、轮询、函数式辅助工具和泛型类型令牌；无运行时依赖 | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-base) |
| `kit-json` | 基于 Jackson 和 Gson 的序列化、转换及结构比较 | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-json) |
| `kit-mybatis` | 支持声明类型与多态类型的 MyBatis JSON 类型处理器 | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-mybatis) |
| `kit-selenium` | Chrome 启动与 WebDriver 生命周期管理 | [Maven Central](https://central.sonatype.com/artifact/io.allurx.kit/kit-selenium) |

## 安装

通过上表中的 Maven Central 链接查看最新发行版，并复制所选版本的依赖声明。

## 使用说明

- **JSON**：共享操作器采用所用 JSON 库的默认配置。需要明确的结果类型时，使用 `Class<T>` 或 `TypeToken<T>`；基于 `Type` 的反序列化和转换返回 `Object`。可通过 `with(mapper -> mapper.rebuild()...build())` 或 `with(gson -> gson.newBuilder()...create())` 自定义配置，并使用返回的新操作器。命名模块需按实际使用的 JSON 库，向 `tools.jackson.databind` 或 `com.google.gson` 开放模型包。见 [JsonOperator](kit-json/src/main/java/io/allurx/kit/json/JsonOperator.java)。
- **MyBatis**：`SimpleJsonTypeHandler` 使用 Jackson 默认配置；`GenericJsonTypeHandler` 启用 Jackson 的 `NON_FINAL` 默认类型机制，要求显式提供 `PolymorphicTypeValidator`。通过 `handler.registerTo(configuration.getTypeHandlerRegistry())` 注册处理器。参数化类型共用原始类作为注册键；元素类型不同时，应显式配置属性映射。见[处理器契约](kit-mybatis/src/main/java/io/allurx/kit/mybatis/handler/AbstractJsonTypeHandler.java)。
- **Chrome**：使用 try-with-resources 关闭实例。`ATTACH` 模式会启动由 Kit 管理的浏览器，应为它使用独立的用户数据目录。见 [Chrome](kit-selenium/src/main/java/io/allurx/kit/selenium/Chrome.java)。

### 注意事项

MyBatis 处理器通过 JDBC `setString` / `getString` 读写数据；数据库原生 JSON 列可能需要 SQL 类型转换。修改模型类名、声明类型或子类型策略，可能使已存储的多态 JSON 无法读取；处理器不会迁移已有数据。类型元信息和子类型策略的约束见 [GenericJsonTypeHandler](kit-mybatis/src/main/java/io/allurx/kit/mybatis/handler/GenericJsonTypeHandler.java)。

## 构建

使用 Maven 3.9.x 和 JDK 25 或更高版本，在仓库根目录运行 `mvn verify`；运行 `mvn install` 可让其他本地工程使用构建后的 Kit 制品。指定模块的构建、源码包与 Javadoc 检查，以及按需启用的 Chrome 启动测试，见[开发说明](docs/development.md)。

CI 和发布流程使用 [allurx-build](https://github.com/allurx/allurx-build)。依赖更新采用共享的 [Renovate 策略](https://github.com/allurx/allurx-build/blob/main/docs/dependency-updates.md)，目标分支为 `dev`。

## 许可证

[Apache License 2.0](LICENSE.txt)
