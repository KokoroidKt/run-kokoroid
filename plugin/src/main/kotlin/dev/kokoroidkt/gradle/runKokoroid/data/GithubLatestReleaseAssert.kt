package dev.kokoroidkt.gradle.runKokoroid.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubLatestReleaseAssert(
    @SerialName("url") var url: String? = null,
    @SerialName("id") var id: Int? = null,
    @SerialName("node_id") var nodeId: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("label") var label: String? = null,
    @SerialName("uploader") var uploader: GithubLatestReleaseUploader? = GithubLatestReleaseUploader(),
    @SerialName("content_type") var contentType: String? = null,
    @SerialName("state") var state: String? = null,
    @SerialName("size") var size: Int? = null,
    @SerialName("digest") var digest: String? = null,
    @SerialName("download_count") var downloadCount: Int? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("browser_download_url") var browserDownloadUrl: String? = null,
)
