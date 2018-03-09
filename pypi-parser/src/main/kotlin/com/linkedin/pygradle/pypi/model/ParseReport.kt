package com.linkedin.pygradle.pypi.model

import java.io.File

data class ParseReport(val name: String, val version: String,
                       val type: PackageType, val file: File, val dependencies: Collection<Dependency>)
