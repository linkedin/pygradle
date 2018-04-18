package com.linkedin.pygradle.pypi.exception

import java.nio.file.Path

class DownloadedArtifactWasNotValidException(f: Path, s: String)
    : RuntimeException("$f doesn't match expected md5 ($s)")
