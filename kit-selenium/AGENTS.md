# Selenium 模块指引

- 本模块管理真实浏览器、driver、调试端口和进程；修改启动或关闭逻辑时，覆盖成功、超时、中断及构建失败后的资源释放。
- TelegramTest 和 src/main/resources/windows 下的启动脚本会使用持久 Chrome profile 并访问外部服务。常规验证须显式选择已检查的受控测试，不能直接运行模块或整个 reactor 的默认 test / verify。
- 真实浏览器验证使用独立临时 profile；不要把示例中的本机路径当作可移植测试夹具，也不要清理用户已有 profile。
- kill-chrome-and-driver.bat 会按进程名终止所有 Chrome / ChromeDriver，不能作为常规测试清理；清理应限定为本次验证创建并拥有的进程。
- 测试位于独立 JPMS 模块；反射测试的开放范围由 pom.xml 的定向 --add-opens 管理，不用关闭 module path 或扩大生产模块开放范围绕过问题。
- 使用本机 Java 子进程和 loopback socket 的测试，只覆盖启动协调逻辑；真实 Chrome / driver、profile 与外部服务行为需要另外验证。
