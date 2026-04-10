plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.gradle.plugin-publish") version "1.2.1"
    `java-gradle-plugin`
    signing
    `maven-publish`
}

group = "dev.kokoroidkt"
version = findProperty("version")?.toString()
    ?: System.getenv("VERSION")
            ?: "undefined"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    val useProxy =
        (project.findProperty("P")?.toString()?.toBoolean() ?: false) ||
            (project.findProperty("useProxy")?.toString()?.toBoolean() ?: false)
    if (useProxy) {
        val proxyHost = project.findProperty("proxyHost")?.toString() ?: "localhost"
        val proxyPort = project.findProperty("proxyPort")?.toString() ?: "7890"
        val proxyProtocol = project.findProperty("proxyProtocol")?.toString() ?: "http"

        systemProperty("kokoroid.proxy.host", proxyHost)
        systemProperty("kokoroid.proxy.port", proxyPort)
        systemProperty("kokoroid.proxy.protocol", proxyProtocol)
    }
}

gradlePlugin {
    website = "https://kokoroidkt.dev/"
    vcsUrl = "https://github.com/kokoroidKt/run-kokoroid.git"
    plugins {
        create("runKokoroid") {
            id = "dev.kokoroidkt.gradle.runKokoroid"
            displayName = "Run Kokoroid"
            description = "Run Kokoroid With Gradle, and get the IDEA Debug Support"
            implementationClass = "dev.kokoroidkt.gradle.runKokoroid.RunKokoroid"
            tags = listOf("Kokoroid", )
        }
    }
}

signing {
    val signingKey: String? = System.getenv("GPG_PRIVATE_KEY")
    val signingPassword: String? = System.getenv("GPG_PASSPHRASE")
    if (signingKey != null && signingPassword != null) {
        println("Using signing key from environment variables")
        useInMemoryPgpKeys(signingKey, signingPassword)
    }else {
        println("Using signing key from local file")
        useGpgCmd()
    }

    sign(publishing.publications)
}