package dev.kokoroidkt.gradle.runKokoroid.http

import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.data.GithubLatestReleaseResponse
import dev.kokoroidkt.gradle.runKokoroid.exceptions.GithubApiFetchFailedException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.readBytes

fun getClient(): HttpClient =
    HttpClient(CIO) {
        engine {
            if (RunKokoroidConfig.proxyHost != null && RunKokoroidConfig.proxyPort != null) {
                proxy =
                    ProxyBuilder.http(
                        Url("${RunKokoroidConfig.proxyProtocol}://${RunKokoroidConfig.proxyHost}:${RunKokoroidConfig.proxyPort}"),
                    )
            }
        }
        install(ContentNegotiation) {
            json()
        }
        headers {
            if (RunKokoroidConfig.githubToken != null) {
                append("Authorization", "Bearer ${RunKokoroidConfig.githubToken}")
            }
        }
    }

data class DownloadInfo(
    val downloadUrl: String?,
    val version: String?,
    val hash: String? = null,
)

fun getLatestKokoroidRelease(client: HttpClient = getClient()): DownloadInfo =
    runBlocking {
        val response = client.get("https://api.github.com/repos/kokoroidKt/kokoroid/releases/latest")
        if (response.status.value != 200) {
            throw GithubApiFetchFailedException(
                "Failed to get latest release, status code: ${response.status.value}, body: ${response.bodyAsText()}",
            )
        }
        val json = response.body<GithubLatestReleaseResponse>()
        val assert = json.assets.first()
        return@runBlocking DownloadInfo(
            assert.browserDownloadUrl,
            json.tagName,
            assert.digest,
        )
    }

fun downloadKokoroid(
    downloadInfo: DownloadInfo,
    savePath: Path = RunKokoroidConfig.kokoroidTempPath,
    client: HttpClient = getClient(),
) {
    val file = savePath.resolve(Path.of("kokoroid-core.jar"))

    if (downloadInfo.hash != null && file.exists()) {
        val currentHash = calculateHash(file)
        if (currentHash.equals(downloadInfo.hash, ignoreCase = true)) {
            println("Existing file matches hash. Skipping download.")
            return
        }
    }

    runBlocking {
        downloadInfo.downloadUrl?.let { url ->
            val response =
                client.get(url) {
                    header("Accept", "application/octet-stream")
                }
            val contentLength = response.headers["Content-Length"]?.toLongOrNull()
            println("Starting download. Total size: ${contentLength ?: "unknown"} bytes")

            val channel = response.bodyAsChannel()
            val output = file.toFile().outputStream()
            val buffer = ByteArray(8192) // 8KB chunks
            var totalRead = 0L

            while (true) {
                val read = channel.readAvailable(buffer, 0, buffer.size)
                if (read == -1) break
                output.write(buffer, 0, read)
                totalRead += read

                if (contentLength != null) {
                    val progress = (totalRead * 100 / contentLength).toInt()
                    println("Progress: $progress% ($totalRead/$contentLength bytes)")
                } else {
                    println("Downloaded: $totalRead bytes")
                }
            }
            output.close()
            println("Download completed.")

            if (downloadInfo.hash != null) {
                val newHash = calculateHash(file)
                if (!newHash.equals(downloadInfo.hash, ignoreCase = true)) {
                    file.toFile().delete()
                    throw RuntimeException("Hash mismatch! Expected: ${downloadInfo.hash}, Actual: $newHash")
                }
                println("Hash verified successfully.")
            }
            true
        } ?: false
    }
}

private fun calculateHash(path: Path): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(path.readBytes())
    return digest.joinToString("") { "%02x".format(it) }
}
