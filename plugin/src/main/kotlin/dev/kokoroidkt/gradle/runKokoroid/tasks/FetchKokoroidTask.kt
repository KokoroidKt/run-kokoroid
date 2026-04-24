package dev.kokoroidkt.gradle.runKokoroid.tasks

import dev.kokoroidkt.gradle.runKokoroid.RunKokoroid
import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.http.downloadKokoroid
import dev.kokoroidkt.gradle.runKokoroid.http.getLatestKokoroidRelease
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class FetchKokoroidTask : DefaultTask() {
    @TaskAction
    fun fetchKokoroid() {
        if (RunKokoroidConfig.skipDownload) {
            println("Skip Download Enabled, Skipped")
            return
        }
        println("Fetching Kokoroid...")
        val release = getLatestKokoroidRelease()
        println("Latest Kokoroid Release: version=${release.version}, url=${release.downloadUrl}, hash=${release.hash}")
        downloadKokoroid(release)
        println("Kokoroid downloaded successfully.")
    }
}
