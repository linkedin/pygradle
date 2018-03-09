package com.linkedin.pygradle.pypi.factory

import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

object PythonVersionFactory {
    @JvmStatic
    fun makeVersion(version: String): PythonPackageVersion = DefaultPythonPackageVersion(version)
}
