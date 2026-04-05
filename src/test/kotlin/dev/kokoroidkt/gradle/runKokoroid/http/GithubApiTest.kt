package dev.kokoroidkt.gradle.runKokoroid.http
import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.exceptions.GithubApiFetchFailedException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.test.*
import kotlin.test.Ignore

class GithubApiTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGetLatestReleaseSuccess() {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content =
                        """
                        {
                          "tag_name": "v1.0.0",
                          "author": {},
                          "assets": [
                            {
                              "browser_download_url": "https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip",
                              "uploader": {}
                            }
                          ]
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        val client =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        },
                    )
                }
            }

        val result = getLatestKokoroidRelease(client)
        assertNotNull(result)
        assertEquals("v1.0.0", result.version)
        assertEquals("https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip", result.downloadUrl)
    }

    @Test
    fun testGetLatestReleaseForbidden() {
        val mockEngine =
            MockEngine { request ->
                respond(
                    content =
                        """
                        {
                          "message": "API rate limit exceeded",
                          "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.Forbidden,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }

        val client =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        },
                    )
                }
            }

        assertFailsWith<GithubApiFetchFailedException> {
            getLatestKokoroidRelease(client)
        }
    }

    @Test
    @Ignore
    fun testGetLatestReleaseReal() {
        // This is a real request test
        try {
            val result = getLatestKokoroidRelease()
            assertNotNull(result)
            println("Latest Kokoroid Release: version=${result.version}, url=${result.downloadUrl}, hash=${result.hash}")
        } catch (e: GithubApiFetchFailedException) {
            if (e.message?.contains("403") == true) {
                println("Rate limited by GitHub (403), skipping assertion on content.")
            } else {
                throw e
            }
        } catch (e: Exception) {
            println("Real request failed with exception: ${e.message}")
            // Don't fail the build if the network is unavailable
        }
    }

    @Test
    fun testDownloadKokoroidMock() {
        val tempDir = createTempDirectory("kokoroid-test")
        try {
            val jarContent = "mock jar content".toByteArray()
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = jarContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString()),
                    )
                }

            val client = HttpClient(mockEngine)
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0")

            downloadKokoroid(downloadInfo, tempDir, client)

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            assertTrue(downloadedFile.exists(), "Downloaded file should exist")
            val downloadedContent = downloadedFile.toFile().readBytes()
            assertEquals(jarContent.size, downloadedContent.size, "File size should match")
            assertContentEquals(jarContent, downloadedContent, "File content should match")
        } finally {
            try {
                tempDir.toFile().deleteRecursively()
            } catch (e: Exception) {
                println("Failed to cleanup temp directory: ${e.message}")
            }
        }
    }

    @Test
    fun testDownloadKokoroidWithHashMatch() {
        val tempDir = createTempDirectory("kokoroid-hash-match")
        try {
            val jarContent = "mock jar content".toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(jarContent).joinToString("") { "%02x".format(it) }

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = jarContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString()),
                    )
                }

            val client = HttpClient(mockEngine)
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0", hash)

            // First download (real download)
            downloadKokoroid(downloadInfo, tempDir, client)

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            assertTrue(downloadedFile.exists())

            // Second download (should skip)
            // If it doesn't skip, it will call mockEngine again.
            // We can verify this by using a counter if needed, but the println will also show.
            downloadKokoroid(downloadInfo, tempDir, client)

            assertTrue(downloadedFile.exists())
            assertContentEquals(jarContent, downloadedFile.toFile().readBytes())
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun testDownloadKokoroidWithHashMismatch() {
        val tempDir = createTempDirectory("kokoroid-hash-mismatch")
        try {
            val jarContent = "mock jar content".toByteArray()
            val wrongHash = "wronghash123"

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = jarContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString()),
                    )
                }

            val client = HttpClient(mockEngine)
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0", wrongHash)

            assertFailsWith<RuntimeException> {
                downloadKokoroid(downloadInfo, tempDir, client)
            }

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            assertFalse(downloadedFile.exists(), "File should be deleted on hash mismatch")
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun testDownloadKokoroidSkipIfHashMatches() {
        val tempDir = createTempDirectory("kokoroid-skip")
        try {
            val jarContent = "existing content".toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(jarContent).joinToString("") { "%02x".format(it) }

            val file = tempDir.resolve("kokoroid-core.jar")
            file.writeBytes(jarContent)

            // Mock engine that fails if called
            val mockEngine =
                MockEngine { request ->
                    fail("Mock engine should not be called when file already exists and hash matches")
                }

            val client = HttpClient(mockEngine)
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0", hash)

            downloadKokoroid(downloadInfo, tempDir, client)

            assertTrue(file.exists())
            assertContentEquals(jarContent, file.readBytes())
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    @Ignore
    fun testDownloadKokoroidReal() {
        val tempDir = createTempDirectory("kokoroid-real-test")
        try {
            val downloadInfo =
                try {
                    getLatestKokoroidRelease()
                } catch (e: Exception) {
                    println("Skipping real download test because failed to get latest release: ${e.message}")
                    return
                }

            assertNotNull(downloadInfo.downloadUrl, "Download URL should not be null")

            downloadKokoroid(downloadInfo, tempDir)

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            if (downloadedFile.exists()) {
                println("Successfully downloaded Kokoroid from ${downloadInfo.downloadUrl}")
                assertTrue(downloadedFile.toFile().length() > 0, "Downloaded file should not be empty")
                if (downloadInfo.hash != null) {
                    println("Verifying hash of downloaded file...")
                    val md = MessageDigest.getInstance("SHA-256")
                    val actualHash = md.digest(downloadedFile.readBytes()).joinToString("") { "%02x".format(it) }
                    assertEquals(downloadInfo.hash?.lowercase(), actualHash.lowercase(), "Downloaded file hash should match")
                    println("Hash verification successful: $actualHash")
                }
            } else {
                println("Real download failed or skipped due to network issues.")
            }
        } catch (e: Exception) {
            println("Real download failed with exception: ${e.message}")
        } finally {
            try {
                tempDir.toFile().deleteRecursively()
            } catch (e: Exception) {
                println("Failed to cleanup temp directory: ${e.message}")
            }
        }
    }
}
