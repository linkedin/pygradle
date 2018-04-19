package com.linkedin.pygradle.pypi.service

import com.linkedin.pygradle.pypi.exception.NoVersionAvailableException
import com.linkedin.pygradle.pypi.internal.http.PackageDetails
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

/**
 * PyPi for Package Details
 */
interface PyPiPackageDetails {
    /**
     * Get a list of versions
     */
    fun getVersion(): List<PythonPackageVersion>

    /**
     * Find the latest version
     */
    fun findLatestVersion(): PythonPackageVersion?

    /**
     * Return the latest version or throw [NoVersionAvailableException]
     */
    @Throws(NoVersionAvailableException::class)
    fun getLatestVersion(): PythonPackageVersion

    /**
     * Get the name
     */
    fun getPackageName(): String

    /**
     * Get the package info
     */
    fun getPackageInfo(): PackageDetails
}
