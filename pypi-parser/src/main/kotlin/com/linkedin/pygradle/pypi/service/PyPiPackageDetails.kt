package com.linkedin.pygradle.pypi.service

import com.linkedin.pygradle.pypi.internal.http.PackageDetails
import com.linkedin.pygradle.pypi.model.PythonPackageVersion

interface PyPiPackageDetails {
    fun getVersion(): List<PythonPackageVersion>
    fun getLatestVersion(): PythonPackageVersion?
    fun getPackageName(): String
    fun getPackageInfo(): PackageDetails
}
