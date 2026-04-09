package dev.kokoroidkt.gradle.runKokoroid.http

import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.data.GithubLatestReleaseResponse
import dev.kokoroidkt.gradle.runKokoroid.exceptions.GithubApiFetchFailedException
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes

private val json =
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

fun getClient(): HttpClient {
    val builder =
        HttpClient
            .newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)

    if (RunKokoroidConfig.proxyHost != null && RunKokoroidConfig.proxyPort != null) {
        val proxyHost = RunKokoroidConfig.proxyHost!!
        val proxyPort = RunKokoroidConfig.proxyPort!!
        builder.proxy(ProxySelector.of(InetSocketAddress(proxyHost, proxyPort)))
    }

    return builder.build()
}

data class DownloadInfo(
    val downloadUrl: String?,
    val version: String?,
    val hash: String? = null,
)

fun getLatestKokoroidRelease(client: HttpClient = getClient()): DownloadInfo {
    val requestBuilder =
        HttpRequest
            .newBuilder()
            .uri(URI.create("https://api.github.com/repos/kokoroidKt/kokoroid/releases/latest"))
            .header("Accept", "application/json")

    if (RunKokoroidConfig.githubToken != null) {
        requestBuilder.header("Authorization", "Bearer ${RunKokoroidConfig.githubToken}")
    }

    val request = requestBuilder.GET().build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() != 200) {
        throw GithubApiFetchFailedException(
            "Failed to get latest release, status code: ${response.statusCode()}, body: ${response.body()}",
        )
    }

    val releaseResponse = json.decodeFromString<GithubLatestReleaseResponse>(response.body())
    val asset = releaseResponse.assets.first()
    return DownloadInfo(
        asset.browserDownloadUrl,
        releaseResponse.tagName,
        asset.digest,
    )
}

fun downloadKokoroid(
    downloadInfo: DownloadInfo,
    savePath: Path = RunKokoroidConfig.kokoroidTempPath,
    client: HttpClient = getClient(),
) {
    savePath.createDirectories()

    val actualSavePath = savePath.toAbsolutePath().normalize()
    actualSavePath.createDirectories()

    val file = actualSavePath.resolve("kokoroid-core.jar")
    file.parent?.createDirectories()

    if (downloadInfo.hash != null && file.exists()) {
        val currentHash = calculateHash(file)
        if (currentHash.equals(downloadInfo.hash, ignoreCase = true)) {
            println("Existing file matches hash. Skipping download.")
            return
        }
    }

    val url = downloadInfo.downloadUrl ?: throw RuntimeException("Download URL is null")
    val requestBuilder =
        HttpRequest
            .newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/octet-stream")

    if (RunKokoroidConfig.githubToken != null && url.contains("api.github.com")) {
        requestBuilder.header("Authorization", "Bearer ${RunKokoroidConfig.githubToken}")
    }

    val request = requestBuilder.GET().build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())

    if (response.statusCode() != 200) {
        throw RuntimeException("Failed to download file, status code: ${response.statusCode()}")
    }

    val contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L)

    println("Starting download. Total size: ${if (contentLength != -1L) contentLength else "unknown"} bytes")
    println("Save path: ${actualSavePath.toAbsolutePath().normalize()}")
    println("Output file: ${file.toAbsolutePath().normalize()}")

    response.body().use { inputStream ->
        file.toFile().outputStream().use { outputStream ->
            val buffer = ByteArray(8192)
            var totalRead = 0L

            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                outputStream.write(buffer, 0, read)
                totalRead += read

                if (contentLength != -1L) {
                    val progress = (totalRead * 100 / contentLength).toInt()
                    print("\rProgress: $progress% ($totalRead/$contentLength bytes)")
                } else {
                    print("\rDownloaded: $totalRead bytes")
                }
            }
        }
    }

    println("\nDownload completed.")

    if (downloadInfo.hash != null) {
        val newHash = calculateHash(file)
        if (!newHash.equals(downloadInfo.hash, ignoreCase = true)) {
            file.toFile().delete()
            throw RuntimeException("Hash mismatch! Expected: ${downloadInfo.hash}, Actual: $newHash")
        }
        println("Hash verified successfully.")
    }
}

private fun calculateHash(path: Path): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(path.readBytes())
    return "sha256:" + digest.joinToString("") { "%02x".format(it) }
}
