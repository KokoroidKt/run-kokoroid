# Run Kokoroid

[English](README.md)/[简体中文](./README_zhCN.md)

Run kokoroid是一个Gradle插件，用于在Gradle调试你的[Kokoroid](https://kokoroidkt.dev)拓展

灵感来源：[jpenilla/run-task](https://github.com/jpenilla/run-task)

## 使用

1. 引入run-Kokoroid插件
```kotlin
plugins {
    id("dev.kokoroidkt.gradle.runKokoroid") version "0.4.0"
}
```
2. 配置run-Kokoroid配置
```kotlin
// 你基本只需要配置这些东西
runKokoroid {
    testExtensionType = ExtensionTypes.DRIVER // 必填:测试拓展类型,
    
    githubToken = System.getenv("GITHUB_TOKEN") // 你的Github Token,如果你被限制了API调用次数,配置这个会很有用
    isValidationOnly = true // 是否只执行验证流程,和正常启动Kokoroid的--validationOnly选项一致
    enableKokoroidDebug = true // 是否开启kokoroid调试模式,和正常启动Kokoroid的--debug选项一致
}
```
3. 使用`runKokoroid`任务启动Kokoroid

## 配置

你可以通过`runKokoroid`拓展函数修改run-kokoroid的配置，以下是你可以配置的内容：

```kotlin
object RunKokoroidConfig {
    /**
     * Github Token
     * 如果遇到访问速率问题，设置他
     * 务必使用环境变量来设置
     */
    var githubToken: String? = null


    /**
     * 拓展文件名
     * 默认使用工件名称，但是如果你发现出现了文件，手动设置此项为构建产物的文件名
     * 如果需要shadowJar，把这项修改为shadowJar产物的名称
     */
    var extensionFilename: String? = null

    /**
     * Kokoroid临时路径
     * Kokoroid-core将运行在此目录下
     * **记得**把他加进 .gitgnore
     */
    val kokoroidTempPath: Path = kotlin.io.path.Path(".kokoroid-temp")

    /**
     * 代理host，如果访问Github网络环境不佳可使用
     */
    var proxyHost: String? = System.getProperty("kokoroid.proxy.host")

    /**
     * 代理端口，如果访问Github网络环境不佳可使用
     */
    var proxyPort: Int? = System.getProperty("kokoroid.proxy.port")?.toIntOrNull()

    /**
     * 是否开启--validation-only flag
     * 不会开启主循环，Kokoroid仅验证插件是否能被正确加载
     */
    var isValidationOnly: Boolean = false

    /**
     * 是否开启--debug flag
     * 这会使Kokoroid开启调试模式
     */
    var enableKokoroidDebug: Boolean = false

    /**
     * 拓展类型
     * 可以是ExtensionTypes.DRIVER，ExtensionTypes.PLUGIN，ExtensionTypes.ADAPTER
     */
    var testExtensionType: ExtensionTypes? = null

    /**
     * 构建产物输出目录
     * 遵循gradle约定，为build/libs
     * 如果需要修改为build目录下的其他文件夹，修改它
     */
    var libDir: String = "libs"

    /**
     * 跳过Kokoroid下载
     * 如果网络环境实在太差，或者想要使用其他Kokoroid版本，启用此选项
     * 此时你需要手动把名为`kokoroid-core.jar`的Kokoroid Core放到kokoroid-temp文件夹
     */
    var skipDownload: Boolean = false
}
```

## 许可证

run-kokoroid使用LGPL-2.1许可证,了解更多请访问[Wikipedia - LGPL2.1](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License#Version_2.1)
