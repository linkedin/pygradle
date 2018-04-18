package com.linkedin.pygradle.pypi.model

/**
 * Interface for Python Package Versioning
 */
interface PythonPackageVersion {

    /**
     * Get the package version string
     */
    fun toVersionString(): String

    /**
     * True is the version is a wildcard
     */
    fun isWildcardVersion(): Boolean

    /**
     * Get the release component
     */
    fun getRelease(): String

    /**
     * Get the post component
     */
    fun getPost(): String?

    /**
     * Get the dev component
     */
    fun getDev(): String?

    /**
     * Get the local component
     */
    fun getLocal(): String?

    /**
     * Get the epoch component
     */
    fun getEpoch(): String?

    /**
     * Get the groups component
     */
    fun getGroups(): List<String?>
}
