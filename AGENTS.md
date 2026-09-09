# Kit 项目指引

## 模块与契约
- Kit 是面向外部消费者的 Java / JPMS 工具库；Java 基线及依赖版本以根 pom.xml 为准。构建前用 mvn -version 核对 Maven 实际使用的 JDK。
- kit-base 承载基础工具且没有生产第三方依赖；kit-json 依赖 base；kit-mybatis 基于 json 提供数据库类型处理；kit-selenium 基于 base 封装浏览器。
- 修改公共 API 或依赖时，同时审查 POM 与 main/test module-info.java。暴露其他模块类型时，检查消费者可读性；涉及模块契约的变更须验证最小独立 named-module consumer，不以 reactor 测试或 classpath 运行替代。
- JSON 默认 operator 是共享对象，configure 会修改当前 backend；with 返回新 wrapper 不保证 backend 已复制。局部配置和测试须使用独立 backend，避免污染共享默认值。
- MyBatis handler 的 JSON 是数据库存储格式。修改默认 JSON 配置、类型信息或模型命名时，核对旧值读取、SQL NULL、异常和 JDBC 行为；多态反序列化还须检查输入信任边界。

## 验证
- 从仓库根选择模块，并使用 -am 同时构建 reactor 依赖；避免仅构建子模块而误用本地仓库中的旧版 Kit。
- 核心模块验证命令：mvn -pl kit-mybatis -am verify。它覆盖 base、json、mybatis 的构建及已有测试，不证明 MyBatis 的实际数据库兼容性，也不覆盖 Selenium。
- 涉及 Selenium 或运行整个 reactor 的测试前，先读 kit-selenium/AGENTS.md，明确测试是否启动真实浏览器、使用持久 profile 或访问外部服务。
- 修改轮询行为时，使用已有 Clock / Sleeper 注入点验证次数、截止与异常边界；修改反射泛型 API 时，同时验证外部调用的编译类型和运行行为。
- JMH 与 JUnit 独立；普通测试通过不能作为性能证据。

## 发布
- 普通验证不启用 release profile；该 profile 另行生成 sources / Javadoc 并在 verify 阶段签名。发布相关变更须核验这些实际产物。
- Central 上传、校验和公开发布分别核实；以 pom.xml 中发布插件配置及最终可下载组件为准，不能只凭 deploy 成功判断公开发布完成。
