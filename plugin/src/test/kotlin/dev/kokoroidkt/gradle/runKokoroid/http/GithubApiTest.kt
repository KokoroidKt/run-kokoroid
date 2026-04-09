package dev.kokoroidkt.gradle.runKokoroid.http

import dev.kokoroidkt.gradle.runKokoroid.config.RunKokoroidConfig
import dev.kokoroidkt.gradle.runKokoroid.exceptions.GithubApiFetchFailedException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional
import java.util.function.BiPredicate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import kotlin.io.path.*
import kotlin.test.*

class GithubApiTest {
    private class MockHttpResponse<T>(
        private val statusCode: Int,
        private val body: T,
        private val headers: Map<String, List<String>> = emptyMap(),
    ) : HttpResponse<T> {
        override fun statusCode(): Int = statusCode

        override fun request(): HttpRequest? = null

        override fun previousResponse(): Optional<HttpResponse<T>> = Optional.empty()

        override fun headers(): java.net.http.HttpHeaders =
            java.net.http.HttpHeaders
                .of(headers) { _, _ -> true }

        override fun body(): T = body

        override fun sslSession(): Optional<javax.net.ssl.SSLSession> = Optional.empty()

        override fun uri(): URI? = null

        override fun version(): HttpClient.Version = HttpClient.Version.HTTP_1_1
    }

    private class MockHttpClient(
        private val handler: (HttpRequest) -> HttpResponse<*>,
    ) : HttpClient() {
        override fun <T : Any?> send(
            request: HttpRequest,
            responseBodyHandler: HttpResponse.BodyHandler<T>,
        ): HttpResponse<T> {
            val response = handler(request)
            @Suppress("UNCHECKED_CAST")
            return response as HttpResponse<T>
        }

        // Unused methods
        override fun cookieHandler(): Optional<java.net.CookieHandler> = Optional.empty()

        override fun connectTimeout(): Optional<java.time.Duration> = Optional.empty()

        override fun followRedirects(): Redirect = Redirect.NEVER

        override fun proxy(): Optional<java.net.ProxySelector> = Optional.empty()

        override fun sslContext(): SSLContext = SSLContext.getDefault()

        override fun sslParameters(): SSLParameters = SSLParameters()

        override fun authenticator(): Optional<java.net.Authenticator> = Optional.empty()

        override fun version(): Version = Version.HTTP_1_1

        override fun executor(): Optional<java.util.concurrent.Executor> = Optional.empty()

        override fun <T : Any?> sendAsync(
            request: HttpRequest,
            responseBodyHandler: HttpResponse.BodyHandler<T>,
        ): java.util.concurrent.CompletableFuture<HttpResponse<T>> = throw UnsupportedOperationException()

        override fun <T : Any?> sendAsync(
            request: HttpRequest,
            responseBodyHandler: HttpResponse.BodyHandler<T>,
            pushPromiseHandler: HttpResponse.PushPromiseHandler<T>,
        ): java.util.concurrent.CompletableFuture<HttpResponse<T>> = throw UnsupportedOperationException()
    }

    @Test
    fun testGetLatestReleaseSuccess() {
        val client =
            MockHttpClient { request ->
                MockHttpResponse(
                    200,
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
                )
            }

        val result = getLatestKokoroidRelease(client)
        assertNotNull(result)
        assertEquals("v1.0.0", result.version)
        assertEquals("https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip", result.downloadUrl)
    }

    @Test
    fun testGetLatestReleaseForbidden() {
        val client =
            MockHttpClient { request ->
                MockHttpResponse(
                    403,
                    """
                    {
                      "message": "API rate limit exceeded",
                      "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                    }
                    """.trimIndent(),
                )
            }

        assertFailsWith<GithubApiFetchFailedException> {
            getLatestKokoroidRelease(client)
        }
    }

    @Test
    fun testDownloadKokoroidMock() {
        val tempDir = createTempDirectory("kokoroid-test")
        try {
            val jarContent = "mock jar content".toByteArray()
            val client =
                MockHttpClient { request ->
                    MockHttpResponse(
                        200,
                        jarContent.inputStream(),
                        mapOf("Content-Length" to listOf(jarContent.size.toString())),
                    )
                }
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0")

            downloadKokoroid(downloadInfo, tempDir, client)

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            assertTrue(downloadedFile.exists(), "Downloaded file should exist")
            val downloadedContent = downloadedFile.readBytes()
            assertEquals(jarContent.size, downloadedContent.size, "File size should match")
            assertContentEquals(jarContent, downloadedContent, "File content should match")
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun testDownloadKokoroidWithHashMatch() {
        val tempDir = createTempDirectory("kokoroid-hash-match")
        try {
            val jarContent = "mock jar content".toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(jarContent).joinToString("") { "%02x".format(it) }

            val client =
                MockHttpClient { request ->
                    MockHttpResponse(
                        200,
                        jarContent.inputStream(),
                        mapOf("Content-Length" to listOf(jarContent.size.toString())),
                    )
                }
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0", hash)

            // First download (real download)
            downloadKokoroid(downloadInfo, tempDir, client)

            val downloadedFile = tempDir.resolve("kokoroid-core.jar")
            assertTrue(downloadedFile.exists())

            // Second download (should skip)
            downloadKokoroid(downloadInfo, tempDir, client)

            assertTrue(downloadedFile.exists())
            assertContentEquals(jarContent, downloadedFile.readBytes())
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

            val client =
                MockHttpClient { request ->
                    MockHttpResponse(
                        200,
                        jarContent.inputStream(),
                        mapOf("Content-Length" to listOf(jarContent.size.toString())),
                    )
                }
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

            // Mock client that fails if called
            val client =
                MockHttpClient { request ->
                    fail("HttpClient should not be called when file already exists and hash matches")
                }
            val downloadInfo = DownloadInfo("https://example.com/kokoroid.jar", "v1.0.0", hash)

            downloadKokoroid(downloadInfo, tempDir, client)

            assertTrue(file.exists())
            assertContentEquals(jarContent, file.readBytes())
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
