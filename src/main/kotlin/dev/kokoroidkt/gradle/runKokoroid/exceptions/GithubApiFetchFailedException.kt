package dev.kokoroidkt.gradle.runKokoroid.exceptions

import org.gradle.api.GradleException

class GithubApiFetchFailedException(
    message: String? = "",
    cause: Throwable? = null,
) : GradleException(message, cause)
