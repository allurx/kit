# Kit 项目指引

## 模块与契约
- Kit 是面向外部消费者的 Java / JPMS 工具库；根 POM 继承 `io.allurx:maven-parent` 的构建配置，业务依赖、JMH 和项目元数据由 Kit 维护。Java 基线以 effective POM 为准；构建前用 `mvn -version` 核对 Maven 实际使用的 JDK。
- kit-base 承载基础工具且没有生产第三方依赖；kit-json 依赖 base；kit-mybatis 基于 json 提供数据库类型处理；kit-selenium 基于 base 封装浏览器。
- 修改公共 API 或依赖时，同时审查 POM 与 main/test module-info.java。暴露其他模块类型时，检查消费者可读性；涉及模块契约的变更须验证最小独立 named-module consumer，不以 reactor 测试或 classpath 运行替代。
- JSON 默认 operator 是共享对象；局部配置通过 with 配合 Jackson rebuild / Gson newBuilder 创建独立 backend。with 返回新 wrapper 不代表 backend 已复制，配置和测试不得污染共享默认值。
- MyBatis handler 的 JSON 是数据库存储格式。修改默认 JSON 配置、类型信息或模型命名时，核对旧值读取、SQL NULL、异常和 JDBC 行为；多态反序列化还须检查输入信任边界。

## 验证
- 从仓库根选择模块，并使用 -am 同时构建 reactor 依赖；避免仅构建子模块而误用本地仓库中的旧版 Kit。
- 核心模块验证命令：mvn -pl kit-mybatis -am verify。它覆盖 base、json、mybatis 的构建及已有测试，不证明 MyBatis 的实际数据库兼容性，也不覆盖 Selenium。
- 涉及 Selenium 或运行整个 reactor 的测试前，先读 kit-selenium/AGENTS.md，明确测试是否启动真实浏览器、使用持久 profile 或访问外部服务。
- 修改轮询行为时，使用已有 Clock / Sleeper 注入点验证次数、截止与异常边界；修改反射泛型 API 时，同时验证外部调用的编译类型和运行行为。
- JMH 与 JUnit 独立；普通测试通过不能作为性能证据。

## 分支与合并
- 短期功能、修复和依赖更新分支合入 `dev` 时使用 `Squash and merge`，每个 PR 保持一个完整意图。
- `dev` 与 `main` 是长期分支；`dev → main` 的发布 PR 使用 `Create a merge commit`，保留共同祖先，避免后续发布 PR 重复包含已压缩的提交。执行合并前核对 PR 的 base/head，并显式选择对应方式，不依赖 GitHub 默认选项。
- 发布验证完成后，将已验证的 `main` 发布提交同步回 `dev`：已包含时无需合并，可快进时使用 fast-forward，已分叉时使用普通 merge，保留 `dev` 的后续改动；不用 Squash。`dev` 更新后推送并核对 CI，具体步骤见发布文档。

## 发布
- 执行发布前读取 [CI/CD 与发布流程](docs/ci-cd.md#release)。用户明确要求发布指定版本时，按该流程完成本次发布所需的版本修改、验证、提交与推送、合入 main、创建并推送 tag 和结果核实；范围明确时不逐步重复确认。
- Kit 版本更新须同步根项目版本与四个子模块的 Kit parent 版本；外部 `maven-parent` 版本独立维护。
- 创建并推送正式 tag 前，由发布执行者确认最终发布 SHA 对应 `.github/workflows/ci.yml` 的最新 `main` push run 及最新 attempt 成功；PR、dev、手动运行或其他提交的成功结果不能替代。
- 通用接入、Maven 契约和发布/失败恢复流程以 [allurx-build 文档](https://github.com/allurx/allurx-build#readme) 及其链接为准。完成公开核验与 GitHub Release 检查，审核说明并补齐破坏性变更迁移要点后，再报告版本、commit、tag 和发布链接。
