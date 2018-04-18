package com.linkedin.pygradle.pypi.factory

import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

/**
 * Builds Python Versions
 */
object PythonPackageVersionFactory {
    /**
     * Creates a version, making it so people don't depend on the impl
     */
    @JvmStatic
    fun makeVersion(version: String): PythonPackageVersion = DefaultPythonPackageVersion(version)
}
