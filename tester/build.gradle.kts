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
}

kotlin {
    jvmToolchain(21)
}

runKokoroid {
    githubToken = System.getenv("GITHUB_TOKEN")
    testExtensionType = "driver"
}
