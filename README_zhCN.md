# Run Kokoroid

[English](README.md)/[简体中文](./README_zhCN.md)

Run kokoroid是一个Gradle插件，用于在Gradle调试你的[Kokoroid](https://kokoroidkt.dev)拓展

灵感来源：[jpenilla/run-task](https://github.com/jpenilla/run-task)

## 使用

1. 引入run-Kokoroid插件
```kotlin
plugins {
id("dev.kokoroidkt.gradle.runKokoroid")
}
```
2. 配置run-Kokoroid配置
```kotlin
// 你基本只需要配置这些东西
runKokoroid {
    testExtensionType = "driver" // 必填:测试拓展类型,可以为driver, plugin或adapter
    
    githubToken = System.getenv("GITHUB_TOKEN") // 你的Github Token,如果你被限制了API调用次数,配置这个会很有用
    isValidationOnly = true // 是否只执行验证流程,和正常启动Kokoroid的--validationOnly选项一致
    enableKokoroidDebug = true // 是否开启kokoroid调试模式,和正常启动Kokoroid的--debug选项一致
}
```
3. 使用`runKokoroid`任务启动Kokoroid

## 许可证

run-kokoroid使用LGPL-2.1许可证,了解更多请访问[Wikipedia - LGPL2.1](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License#Version_2.1)
