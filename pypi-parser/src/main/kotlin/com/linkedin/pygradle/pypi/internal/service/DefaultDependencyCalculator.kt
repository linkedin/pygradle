package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.exception.*
import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.model.PackageType
import com.linkedin.pygradle.pypi.model.PackageType.Companion.matchPackageType
import com.linkedin.pygradle.pypi.model.ParseReport
import com.linkedin.pygradle.pypi.model.PythonPackageVersion
import com.linkedin.pygradle.pypi.service.DependencyCalculator
import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import com.linkedin.pygradle.pypi.service.PyPiRemote
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FilenameUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal class DefaultDependencyCalculator(private val okHttpClient: OkHttpClient,
                                           private val pyPiRemote: PyPiRemote,
                                           private val cacheFolder: File,
                                           private val typeFilter: (PackageRelease) -> Boolean) : DependencyCalculator {

    private val log = LoggerFactory.getLogger(DefaultDependencyCalculator::class.java)

    override fun calculateDependencies(packageDetails: PyPiPackageDetails, version: PythonPackageVersion): ParseReport {
        val versions = packageDetails.getPackageInfo().releases[version.toVersionString()]
            ?: throw VersionNotSupportedException("Version $version didn't exist")

        for (packageRelease in versions.filter(typeFilter)) {
            return when (packageRelease.matchPackageType()) {
                PackageType.BDIST_WHEEL -> processWheel(packageDetails, version, packageRelease)
                PackageType.S_DIST -> processSdist(packageDetails, version, packageRelease)
                else -> {
                    throw PyPiParserBugException.UnsupportedDistributionTypeException(packageRelease)
                }
            }
        }

        throw NoCompatibleDependencyException()
    }

    private fun processWheel(packageDetails: PyPiPackageDetails, version: PythonPackageVersion, dist: PackageRelease): ParseReport {
        val cacheFile = cacheFolder.toPath().resolve(FilenameUtils.getName(dist.url))
        maybeDownloadFile(cacheFile, dist, packageDetails)

        var json = explodeZipForFile(cacheFile.toFile(),
            "${packageDetails.getPackageName()}-${version.toVersionString()}.dist-info/metadata.json")

        if (json == null) {
            json = explodeZipForFile(cacheFile.toFile(),
                "${packageDetails.getPackageName().replace("-", "_")}-${version.toVersionString()}.dist-info/metadata.json")
        }

        if (json == null) {
            log.warn("Unable to find requires file for {}", packageDetails.getPackageName())
            json = ""
        }

        val dependencies = WheelManifestParser(pyPiRemote).calculateDependencies(json)
        return ParseReport(packageDetails.getPackageName(), version.toVersionString(),
            PackageType.BDIST_WHEEL, cacheFile.toFile(), dependencies)
    }

    private fun processSdist(packageDetails: PyPiPackageDetails,
                             version: PythonPackageVersion,
                             dist: PackageRelease): ParseReport {
        val cacheFile = cacheFolder.toPath().resolve(FilenameUtils.getName(dist.url))
        maybeDownloadFile(cacheFile, dist, packageDetails)

        var requiresTxt = if (dist.url.contains(".tar.")) {
            explodeTarForRequiresText(cacheFile.toFile())
        } else {
            explodeZipForRequiresText(cacheFile.toFile())
        }

        requiresTxt = requiresTxt ?: ""

        log.trace("Processing dependency {}", packageDetails.getPackageName())

        val dependencies = RequiresTxtParser(pyPiRemote).calculateDependencies(requiresTxt)
        return ParseReport(packageDetails.getPackageName(), version.toVersionString(),
            PackageType.S_DIST, cacheFile.toFile(), dependencies)
    }

    private fun maybeDownloadFile(cacheFile: Path, dist: PackageRelease, packageDetails: PyPiPackageDetails) {
        if (!Files.exists(cacheFile)) {
            val sdistCall = okHttpClient.newCall(Request.Builder().get().url(dist.url).build())
            val response = sdistCall.execute()

            if (!response.isSuccessful) {
                throw UnableToMakeHttpRequestException(dist.url)
            }

            val tempFile = Files.createTempFile("sdist_${packageDetails.getPackageName()}", "." + FilenameUtils.getExtension(dist.url))
            val bodyBytes = response.body()?.bytes()
                ?: throw PyPiParserBugException.RequestWasNotSuccessfulException(packageDetails.getPackageName())
            tempFile.toFile().writeBytes(bodyBytes)
            Files.move(tempFile, cacheFile)
        }

        val md5 = cacheFile.toFile().inputStream().use {
            DigestUtils.md5Hex(it)
        }

        if (!dist.md5Digest.equals(md5, ignoreCase = true)) {
            throw PyPiParserBugException.DownloadedArtifactWasNotValidException(cacheFile, dist.md5Digest)
        }
    }
}
