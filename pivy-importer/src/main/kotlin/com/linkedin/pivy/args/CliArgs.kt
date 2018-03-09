package com.linkedin.pivy.args

import com.beust.jcommander.Parameter
import com.beust.jcommander.converters.FileConverter
import java.io.File
import java.util.*


class CliArgs {
    @Parameter(required = true)
    lateinit var dependencies: String

    @Parameter(names = ["-v", "--verbose"], description = "Level of verbosity")
    var verbose = false

    @Parameter(names = ["--repo"],
        description = "Where the Ivy repo will be exported to.",
        converter = FileConverter::class,
        required = true)
    lateinit var repo: File

    @Parameter(names = ["--cache"],
        description = "Directory to cache files from PyPi",
        converter = FileConverter::class,
        required = true)
    lateinit var cache: File

    @Parameter(names = ["-d", "--debug"], description = "Level of verbosity")
    var debug = false

    @Parameter(names = ["--version-overrides"],
        description = "Override graph resolution to be these.",
        converter = VersionForceConverter::class)
    var versionOverrides = HashMap<String, String>()

    @Parameter(names = ["--require-all-versions"],
        description = "Fail of a dependency is processed without a provided version number")
    var requireAllVersions = false


    @Parameter(names = ["--help", "-h"], help = true)
    var help: Boolean = false
}
