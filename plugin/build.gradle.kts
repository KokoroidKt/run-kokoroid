plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    `java-gradle-plugin`
}

group = "dev.kokoroid"
version = "1.0-SNAPSHOT"

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
    plugins {
        create("runKokoroid") {
            id = "dev.kokoroidkt.gradle.runKokoroid"
            implementationClass = "dev.kokoroidkt.gradle.runKokoroid.RunKokoroid"
        }
    }
}
