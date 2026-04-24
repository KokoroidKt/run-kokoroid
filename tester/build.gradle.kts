import dev.kokoroidkt.gradle.runKokoroid.config.runKokoroid
import dev.kokoroidkt.gradle.runKokoroid.config.ExtensionTypes

plugins {
    kotlin("jvm") version "2.3.10"
    id("dev.kokoroidkt.gradle.runKokoroid")
}

group = "dev.kokoroid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // okay.....
    implementation("dev.kokoroidkt:kokoroidkt-driver-api:0.4.0")
    implementation("dev.kokoroidkt:kokoroidkt-core-api:0.4.0")
}

kotlin {
    jvmToolchain(21)
}

runKokoroid {
    githubToken = System.getenv("GITHUB_TOKEN")
    testExtensionType = ExtensionTypes.DRIVER
    isValidationOnly = false
    enableKokoroidDebug = true
    skipDownload = true
    extensionFilename = "tester-1.0-SNAPSHOT.jar"
}
