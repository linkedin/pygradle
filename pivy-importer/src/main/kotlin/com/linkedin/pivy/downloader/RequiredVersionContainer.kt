package com.linkedin.pivy.downloader

internal class RequiredVersionContainer {

    private val versionMap: MutableMap<String, String> = mutableMapOf()

    /**
     * Get a version, throw if it doesn't exist
     */
    fun getVersion(name: String): String {
        return findVersion(name) ?: throw IllegalArgumentException("Unknown dependency `$name`")
    }

    /**
     * Find a version, null if not exist
     */
    fun findVersion(name: String): String? = versionMap[name]

    /**
     * Track a package to version, will use this version from now on.
     */
    fun register(first: String, last: String) {
        versionMap[first] = last
    }
}

