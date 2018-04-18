package com.linkedin.pivy

internal class VersionParseException(s: String) : RuntimeException(s)

internal class NoVersionProvidedException(s: String): RuntimeException(s)

internal class UnsupportedDistributionTypeException(s: String): RuntimeException(s)
