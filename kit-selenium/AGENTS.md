# Selenium 模块指引

- 本模块管理真实浏览器、driver、调试端口和进程；修改启动或关闭逻辑时，覆盖成功、超时、中断及构建失败后的资源释放。
- ChromeStartupTest 仅在设置 kit.selenium.chromePath 时启用，通过公开 API 和临时 profile 验证真实 Chrome 的 ATTACH 启动。常规验证不设置该属性，测试会跳过；跳过不代表真实启动已验证。
- scripts/windows 下的手工脚本不随 JAR 发布；其中的启动脚本使用持久 Chrome profile 并访问外部服务，不能作为常规验证入口。
- 真实浏览器验证使用独立临时 profile；不要把示例中的本机路径当作可移植测试夹具，也不要清理用户已有 profile。
- kill-chrome-and-driver.bat 会按进程名终止所有 Chrome / ChromeDriver，不能作为常规测试清理；清理应限定为本次验证创建并拥有的进程。
- 测试位于独立 JPMS 模块，通过公开 API 验证启动；保持 module path，不为测试扩大生产模块的开放范围。
