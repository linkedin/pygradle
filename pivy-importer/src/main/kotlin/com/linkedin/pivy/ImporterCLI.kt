@file:JvmName("ImporterCLI")

package com.linkedin.pivy

import com.linkedin.pivy.downloader.PackageDownloader
import com.linkedin.pivy.downloader.RequiredVersionBuilder
import com.linkedin.pivy.ivy.IvyRepoHelper
import com.linkedin.pygradle.pypi.model.DependencyOptions
import org.slf4j.LoggerFactory
import java.util.*

private val log = LoggerFactory.getLogger("ImporterCLI")

fun main(args: Array<String>) {
    val options = CliParser.parseArg(args)
    val container = RequiredVersionBuilder.buildRequiredVersion(options)

    val downloader = PackageDownloader(options, container, listOf("sdist"))
    val finishedSet = mutableSetOf<String>()
    val processQueue: Queue<String> = LinkedList<String>()

    log.debug("Options to CLI are {}", options)

    options.repo.mkdirs()
    options.cache.mkdirs()

    processQueue.addAll(options.dependencies)

    log.info("Beginning Import")

    val depFilter = object : DependencyOptions {
        override fun hasPackage(name: String): Boolean = false
        override fun getSystemVersion(name: String): String? = null
    }

    val ivyRepoHelper = IvyRepoHelper(container)

    while (processQueue.isNotEmpty()) {
        val packageName = processQueue.poll()
        try {
            if (finishedSet.contains(packageName)) {
                continue
            }
            log.info("Processing dependency {}", packageName)

            finishedSet.add(packageName)

            val report = downloader.downloadPackage(packageName)
            if (report == null) {
                log.warn("Was not able to process {}", packageName)
                continue
            }

            val unconditionalDependencies = report.dependencies.filter {
                it.extras.any { it.shouldInclude(depFilter) }
            }.toList()

            log.trace("{} is adding the following dependencies {}", packageName, unconditionalDependencies.map { it.name })

            processQueue.addAll(unconditionalDependencies.map { it.name })
            ivyRepoHelper.putPackageInRepo(report, options.repo, unconditionalDependencies)
        } catch (t: Throwable) {
            log.error("Error processing {}", packageName)
            throw Throwable(t)
        } finally {
            log.trace("Finished {}", packageName)
            log.trace("Queue Size: {}", processQueue.size)
        }
    }
}
