package dev.kokoroidkt.gradle.runKokoroid.tasks

import dev.kokoroidkt.gradle.runKokoroid.RunKokoroid
import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.http.downloadKokoroid
import dev.kokoroidkt.gradle.runKokoroid.http.getLatestKokoroidRelease
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.createDirectories

open class FetchKokoroidTask : DefaultTask() {
    @TaskAction
    fun fetchKokoroid() {
        val savePath = RunKokoroidConfig.kokoroidTempPath
        savePath.createDirectories()

        val actualSavePath = savePath.toAbsolutePath().normalize()
        actualSavePath.createDirectories()

        val file = actualSavePath.resolve("kokoroid-core.jar")
        file.parent?.createDirectories()
        println("Temp kokoroid will be at: $actualSavePath")
        if (RunKokoroidConfig.skipDownload) {
            println("Skip Download Enabled, Skipped")
            return
        }
        println("Fetching Kokoroid...")
        val release = getLatestKokoroidRelease()
        println("Latest Kokoroid Release: version=${release.version}, url=${release.downloadUrl}, hash=${release.hash}")
        downloadKokoroid(release, file)
        println("Kokoroid downloaded successfully.")
    }
}
