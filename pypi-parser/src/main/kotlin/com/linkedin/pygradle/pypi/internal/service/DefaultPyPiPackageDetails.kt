package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.internal.http.PackageDetails
import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.internal.model.VersionComparator
import com.linkedin.pygradle.pypi.model.PythonPackageVersion
import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import org.slf4j.LoggerFactory

class DefaultPyPiPackageDetails(private val packageDetails: PackageDetails) : PyPiPackageDetails {

    private val logger = LoggerFactory.getLogger(DefaultPyPiPackageDetails::class.java)!!
    private val versions: List<PythonPackageVersion>

    init {
        logger.trace("Building {}", packageDetails.info.name)
        versions = packageDetails.releases.keys
            .filter { DefaultPythonPackageVersion.isSupportedVersion(it) }
            .map { DefaultPythonPackageVersion(it) }
            .sortedWith(VersionComparator())
    }

    override fun getVersion(): List<PythonPackageVersion> = versions

    override fun getLatestVersion(): PythonPackageVersion? = versions.lastOrNull()

    override fun getPackageName(): String = packageDetails.info.name

    override fun getPackageInfo(): PackageDetails = packageDetails
}
