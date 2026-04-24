# Run Kokoroid

[English](README.md)/[简体中文](./README_zhCN.md)

Run-Kokoroid is a Gradle plugin for debugging your [Kokoroid](https://kokoroidkt.dev) extensions within Gradle.

Inspired by: https://github.com/jpenilla/run-task

## Usage

1. Apply the run-kokoroid plugin.
```kotlin
plugins {
    id("dev.kokoroidkt.gradle.runKokoroid") version "0.4.0"
}
```
2. Configure the run-kokoroid extension.
```kotlin
// You basically only need to configure these items.
runKokoroid {
   testExtensionType = ExtensionTypes.DRIVER // Required: The type of test extension. 

   githubToken = System.getenv("GITHUB_TOKEN") // Your GitHub Token. This is useful if you are limited by API rate limits.
   isValidationOnly = true // Whether to only perform the validation process. This corresponds to Kokoroid's `--validationOnly` option.
   enableKokoroidDebug = true // Whether to enable Kokoroid debug mode. This corresponds to Kokoroid's `--debug` option.
}
```

3. Use the `runKokoroid` task to start Kokoroid.

## Configuration

You can modify the run-kokoroid configuration through the `runKokoroid` extension function. The following are the configurable options:

```kotlin
object RunKokoroidConfig {
    /**
     * Github Token
     * Set this if you encounter rate limit problems.
     * Make sure to use an environment variable to set it.
     */
    var githubToken: String? = null


    /**
     * Extension filename
     * Defaults to the artifact name, but if a file is missing, manually set this to the filename of the build output.
     * If shadowJar is needed, change this to the name of the shadowJar output.
     */
    var extensionFilename: String? = null

    /**
     * Kokoroid temporary path
     * Kokoroid-core will run in this directory.
     * **Remember** to add it to .gitignore.
     */
    val kokoroidTempPath: Path = kotlin.io.path.Path(".kokoroid-temp")

    /**
     * Proxy host, can be used if network conditions for accessing Github are poor.
     */
    var proxyHost: String? = System.getProperty("kokoroid.proxy.host")

    /**
     * Proxy port, can be used if network conditions for accessing Github are poor.
     */
    var proxyPort: Int? = System.getProperty("kokoroid.proxy.port")?.toIntOrNull()

    /**
     * Whether to enable the --validation-only flag
     * The main loop will not start. Kokoroid will only verify that the plugin can be loaded correctly.
     */
    var isValidationOnly: Boolean = false

    /**
     * Whether to enable the --debug flag
     * This will enable debug mode for Kokoroid.
     */
    var enableKokoroidDebug: Boolean = false

    /**
     * Extension type
     * Can be ExtensionTypes.DRIVER, ExtensionTypes.PLUGIN, or ExtensionTypes.ADAPTER
     */
    var testExtensionType: ExtensionTypes? = null

    /**
     * Build output directory
     * Follows the Gradle convention: build/libs
     * If you need to change it to another folder under the build directory, modify this.
     */
    var libDir: String = "libs"

    /**
     * Skip Kokoroid download
     * Enable this option if the network environment is extremely poor, or if you want to use a different version of Kokoroid.
     * In this case, you need to manually place a Kokoroid Core named `kokoroid-core.jar` in the kokoroid-temp folder.
     */
    var skipDownload: Boolean = false
}
```

## License

run-kokoroid is licensed under the LGPL-2.1 License. For more information, please visit [Wikipedia - LGPL V2.1](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License#Version_2.1).