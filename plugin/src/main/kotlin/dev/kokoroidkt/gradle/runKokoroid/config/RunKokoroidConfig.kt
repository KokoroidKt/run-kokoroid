package dev.kokoroidkt.gradle.runKokoroid.config

import dev.kokoroidkt.gradle.runKokoroid.RunKokoroid
import java.nio.file.Path
import kotlin.concurrent.atomics.atomicArrayOfNulls

object RunKokoroidConfig {
    /**
     * GitHub API Token
     * If you meet the rate limit, you can set this.
     */
    var githubToken: String? = null

    /**
     *  Kokoroid Version
     *  RunKokoroid will download the latest version if you don't set this.
     */
    val version: String? = null

    /**
     * Kokoroid Temp Path
     * The Jar file & temp Kokoroid configs will put on this folder
     * REMENBER to add it into .gitgnore
     */
    val kokoroidTempPath: Path = kotlin.io.path.Path(".kokoroid-temp")

    /**
     * Proxy Host
     */
    var proxyHost: String? = System.getProperty("kokoroid.proxy.host")

    /**
     * Proxy Port
     */
    var proxyPort: Int? = System.getProperty("kokoroid.proxy.port")?.toIntOrNull()

    /**
     * Proxy Protocol
     */
    var proxyProtocol: String = System.getProperty("kokoroid.proxy.protocol") ?: "http"

    /**
     *
     */
    var useExists: Boolean = false

    var isValidationOnly: Boolean = false

    var enableKokoroidDebug: Boolean = false

    var testExtensionType: String? = null
}

fun runKokoroid(block: RunKokoroidConfig.() -> Unit) {
    RunKokoroidConfig.block()
}
