package com.linkedin.pivy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.beust.jcommander.JCommander
import com.linkedin.pivy.args.CliArgs
import org.slf4j.LoggerFactory
import com.linkedin.pivy.downloader.PackageDownloader

/**
 * Parses the CLI Args
 */
internal object CliParser {
    private val log = LoggerFactory.getLogger(CliParser::class.java)

    /**
     * Parsed the CLI args and provides an [ImporterOptions] as a result.
     */
    fun parseArg(args: Array<String>): ImporterOptions {
        val cliArgs = CliArgs()
        val jCommander = JCommander.newBuilder()
            .addObject(cliArgs)
            .build()
        jCommander.parse(*args)

        if (cliArgs.help) {
            jCommander.usage()
            System.exit(0)
        }

        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        val packageDownloaderLogger = LoggerFactory.getLogger(PackageDownloader::class.java) as Logger
        root.level = Level.INFO

        if (cliArgs.verbose) {
            root.level = Level.DEBUG
            packageDownloaderLogger.level = Level.DEBUG
        }

        if (cliArgs.debug) {
            root.level = Level.TRACE
            packageDownloaderLogger.level = Level.DEBUG
        }

        val dependencies = cliArgs.dependencies.split(",", " ")

        log.debug("CLI Options ({}): {}", dependencies.size, dependencies)

        return ImporterOptions(dependencies, cliArgs.versionOverrides, cliArgs.repo, cliArgs.cache, cliArgs.requireAllVersions)
    }
}
