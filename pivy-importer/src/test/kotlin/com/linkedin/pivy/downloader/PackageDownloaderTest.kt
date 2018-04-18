package com.linkedin.pivy.downloader

import com.linkedin.pivy.ImporterOptions
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

internal class PackageDownloaderTest {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()


    @Test
    internal fun `when version provided, it will be used`() {
        val packageDownloader = makeDependencies(mapOf("flake8" to "2.6.2"), true)
        val report = packageDownloader.downloadPackage("flake8")

        assertNotNull(report)
        assertEquals("2.6.2", report!!.version)
    }

    @Test
    internal fun `when version not provided, and require all versions is true, it will throw`() {
        val packageDownloader = makeDependencies(emptyMap(), true)
        assertFails {
            packageDownloader.downloadPackage("flake8")
        }
    }

    @Test
    internal fun `when version not provided, and require all versions is false, it will resolve`() {
        val packageDownloader = makeDependencies(emptyMap(), false)
        packageDownloader.downloadPackage("flake8")
    }

    @Test
    internal fun `when version is provides, and require all versions is false, it will resolve to specified version`() {
        val packageDownloader = makeDependencies(mapOf("flake8" to "2.6.2"), false)
        val report = packageDownloader.downloadPackage("flake8")

        assertNotNull(report)
        assertEquals("2.6.2", report!!.version)
    }

    private fun makeDependencies(dependencyMap: Map<String, String>, requireAllVersions: Boolean): PackageDownloader {
        val importOptions = ImporterOptions(
            listOf("flake8"),
            dependencyMap,
            temporaryFolder.newFolder("repo"),
            temporaryFolder.newFolder("cache"),
            requireAllVersions)

        val requiredVersionContainer = RequiredVersionContainer()
        importOptions.versionRequirements.forEach { (key, value) -> requiredVersionContainer.register(key, value) }
        return PackageDownloader(importOptions, requiredVersionContainer, listOf("sdist"))
    }
}
