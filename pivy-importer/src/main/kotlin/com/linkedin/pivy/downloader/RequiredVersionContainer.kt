package com.linkedin.pivy.downloader

internal class RequiredVersionContainer {

    private val versionMap: MutableMap<String, String> = mutableMapOf()

    fun getVersion(name: String): String {
        return findVersion(name) ?: throw IllegalArgumentException("Unknown dependency `$name`")
    }

    fun findVersion(name: String): String? = versionMap[name]

    fun register(first: String, last: String) {
        versionMap[first] = last
    }
}

