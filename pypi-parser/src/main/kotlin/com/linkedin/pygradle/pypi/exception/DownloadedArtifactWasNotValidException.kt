package com.linkedin.pygradle.pypi.exception

import java.nio.file.Path

/**
 * The artifact downloaded was not valid
 */
class DownloadedArtifactWasNotValidException(f: Path, s: String)
    : RuntimeException("$f doesn't match expected md5 ($s)")
