plugins {
    kotlin("jvm") version "2.3.10"
    `java-gradle-plugin`
}

group = "dev.kokoroid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}


gradlePlugin {
    plugins {
        create("runKokoroid") {
            id = "dev.kokoroidkt.gradle.runKokoroid.RunKokoroid"
            implementationClass = "org.gradle.sample.SimplePlugin"
        }
    }
}