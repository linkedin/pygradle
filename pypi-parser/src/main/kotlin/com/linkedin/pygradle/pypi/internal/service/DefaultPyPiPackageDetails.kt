package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.exception.NoVersionAvailableException
import com.linkedin.pygradle.pypi.internal.http.PackageDetails
import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.internal.model.VersionComparator
import com.linkedin.pygradle.pypi.model.PythonPackageVersion
import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DefaultPyPiPackageDetails(private val packageDetails: PackageDetails) : PyPiPackageDetails {

    private val logger: Logger = LoggerFactory.getLogger(DefaultPyPiPackageDetails::class.java)
    private val versions: List<PythonPackageVersion>

    init {
        logger.trace("Building {}", packageDetails.info.name)
        versions = packageDetails.releases.keys
            .filter { DefaultPythonPackageVersion.isSupportedVersion(it) }
            .map { DefaultPythonPackageVersion(it) }
            .sortedWith(VersionComparator())
    }

    override fun getVersion(): List<PythonPackageVersion> = versions

    override fun findLatestVersion(): PythonPackageVersion? = versions.lastOrNull()

    override fun getLatestVersion(): PythonPackageVersion {
        return findLatestVersion() ?: throw NoVersionAvailableException(getPackageName())
    }

    override fun getPackageName(): String = packageDetails.info.name

    override fun getPackageInfo(): PackageDetails = packageDetails
}
