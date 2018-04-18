package com.linkedin.pivy.ivy

import com.linkedin.pivy.UnsupportedDistributionTypeException
import com.linkedin.pivy.downloader.RequiredVersionContainer
import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.PackageType
import com.linkedin.pygradle.pypi.model.ParseReport
import org.redundent.kotlin.xml.xml
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

internal class IvyRepoHelper(private val versionContainer: RequiredVersionContainer) {

    /**
     * Puts a given resolved report into the Ivy Repo
     */
    fun putPackageInRepo(report: ParseReport, repoBase: File, deps: List<Dependency>) {
        val configuration = when (report.type) {
            PackageType.S_DIST -> "sdist"
            PackageType.BDIST_WHEEL -> "wheel"
            else -> throw UnsupportedDistributionTypeException("Unsupported Dist Type")
        }

        val ext = listOf("tar.gz", "whl", "zip", "tar.bz2").first { report.file.name.endsWith(it) }

        val name = report.name
        val artifact = mapOf("name" to name, "ext" to ext, "conf" to configuration, "type" to if (ext == "whl") "zip" else ext)

        val ivyText = xml("ivy-module") {
            attribute("version", "2.0")
            attribute("xmlns:e", "http://ant.apache.org/ivy/extra")
            attribute("xmlns:m", "http://ant.apache.org/ivy/maven")
            "info" {
                attribute("organisation", "pypi")
                attribute("module", report.name)
                attribute("revision", report.version)
            }

            "configurations" {
                "conf"("name" to "default",
                    "description" to "auto generated configuration for default",
                    "extends" to configuration)
                "conf"("name" to configuration,
                    "description" to "auto generated configuration for $configuration")
            }

            "publications" {
                "artifact" {
                    artifact.entries.forEach {
                        attribute(it.key, it.value)
                    }
                }
            }

            val node = "dependencies"("defaultconfmapping" to "*->default")
            deps.forEach {
                val dep = node.element("dependency")
                dep.attributes("org" to "pypi",
                    "name" to it.name,
                    "rev" to versionContainer.getVersion(it.name),
                    "conf" to configuration)
            }
        }

        val basePath = repoBase.toPath()
        val versionDir = basePath.resolve(Paths.get("pypi", report.name, report.version))
        versionDir.toFile().mkdirs()

        val artifactPath = versionDir.resolve(report.file.name)

        val ivyFile = File(versionDir.toFile(), "${report.name}-${report.version}.ivy")
        ivyFile.writeText(ivyText.toString())

        Files.copy(report.file.toPath(), artifactPath, StandardCopyOption.REPLACE_EXISTING)
    }
}
