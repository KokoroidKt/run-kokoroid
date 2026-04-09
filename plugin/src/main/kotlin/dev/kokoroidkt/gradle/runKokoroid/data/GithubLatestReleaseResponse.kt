package dev.kokoroidkt.gradle.runKokoroid.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubLatestReleaseResponse(
    @SerialName("url") var url: String? = null,
    @SerialName("assets_url") var assetsUrl: String? = null,
    @SerialName("upload_url") var uploadUrl: String? = null,
    @SerialName("html_url") var htmlUrl: String? = null,
    @SerialName("id") var id: Int? = null,
    @SerialName("author") var author: GithubLatestReleaseAuthor? = GithubLatestReleaseAuthor(),
    @SerialName("node_id") var nodeId: String? = null,
    @SerialName("tag_name") var tagName: String? = null,
    @SerialName("target_commitish") var targetCommitish: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("draft") var draft: Boolean? = null,
    @SerialName("immutable") var immutable: Boolean? = null,
    @SerialName("prerelease") var prerelease: Boolean? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("published_at") var publishedAt: String? = null,
    @SerialName("assets") var assets: ArrayList<GithubLatestReleaseAssert> = arrayListOf(),
    @SerialName("tarball_url") var tarballUrl: String? = null,
    @SerialName("zipball_url") var zipballUrl: String? = null,
    @SerialName("body") var body: String? = null,
)
