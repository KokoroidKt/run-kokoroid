package dev.kokoroidkt.gradle.runKokoroid.config

import dev.kokoroidkt.gradle.runKokoroid.RunKokoroid
import java.nio.file.Path
import kotlin.concurrent.atomics.atomicArrayOfNulls

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

fun runKokoroid(block: RunKokoroidConfig.() -> Unit) {
    RunKokoroidConfig.block()
}
