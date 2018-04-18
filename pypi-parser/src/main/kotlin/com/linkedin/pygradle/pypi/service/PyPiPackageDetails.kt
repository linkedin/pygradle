package com.linkedin.pygradle.pypi.service

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
     * Get the latest version
     */
    fun getLatestVersion(): PythonPackageVersion?

    /**
     * Get the name
     */
    fun getPackageName(): String

    /**
     * Get the package info
     */
    fun getPackageInfo(): PackageDetails
}
