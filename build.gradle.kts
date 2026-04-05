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
    val ktorVersion = "3.4.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("dev.kokoroidkt:kokoroidkt-core:0.3.2")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
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
            id = "dev.kokoroidkt.gradle.runKokoroid.RunKokoroid"
            implementationClass = "org.gradle.sample.SimplePlugin"
        }
    }
}
