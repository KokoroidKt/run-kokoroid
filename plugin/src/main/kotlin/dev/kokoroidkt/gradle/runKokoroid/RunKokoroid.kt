package dev.kokoroidkt.gradle.runKokoroid

import dev.kokoroidkt.gradle.runKokoroid.http.downloadKokoroid
import dev.kokoroidkt.gradle.runKokoroid.http.getLatestKokoroidRelease
import dev.kokoroidkt.gradle.runKokoroid.tasks.FetchKokoroidTask
import dev.kokoroidkt.gradle.runKokoroid.tasks.RunKokoroidTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.cc.base.logger

class RunKokoroid : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("fetchKokoroid", FetchKokoroidTask::class.java) {
            it.group = "kokoroid"
            it.description = "Fetch latest Kokoroid release"
            it.dependsOn("build")
        }

        target.tasks.register("runKokoroid", RunKokoroidTask::class.java) {
            it.group = "kokoroid"
            it.description = "Run Kokoroid"
            it.dependsOn("fetchKokoroid")
        }
    }
}
