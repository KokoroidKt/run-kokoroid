import dev.kokoroidkt.gradle.runKokoroid.config.runKokoroid

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
    implementation("dev.kokoroidkt:kokoroidkt-driver-api:0.3.3")
    implementation("dev.kokoroidkt:kokoroidkt-core-api:0.3.3")
}

kotlin {
    jvmToolchain(21)
}

runKokoroid {
    githubToken = System.getenv("GITHUB_TOKEN")
    testExtensionType = "driver"
    isValidationOnly = true
    enableKokoroidDebug = true
}
