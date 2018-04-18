package com.linkedin.pygradle.pypi.internal.http

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Package Details
 */
data class PackageDetails(
    @JsonProperty("info") val info: PackageInfo,
    @JsonProperty("releases") val releases: Map<String, List<PackageRelease>>
)

/**
 * Package Info
 */
data class PackageInfo(
    @JsonProperty("name") val name: String
)

/**
 * Release Details
 */
data class PackageRelease(
    @JsonProperty("upload_time") val uploadTime: String,
    @JsonProperty("python_version") val pythonVersion: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("packagetype") val packageType: String,
    @JsonProperty("md5_digest") val md5Digest: String
)
