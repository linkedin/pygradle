package com.linkedin.pygradle.pypi.exception

class ArchiveParseException(type: String, packageName: String) : RuntimeException("Unable to parse $type for $packageName")
