package com.linkedin.pivy

import java.io.File

data class ImporterOptions(
    val dependencies: List<String>,
    val versionRequirements: Map<String, String>,
    val repo: File,
    val cache: File,
    val requireAllVersions: Boolean
)
