package com.linkedin.pygradle.pypi.exception

/**
 * There was no latest version for a package
 */
class NoVersionAvailableException(packageName: String) : RuntimeException("Unable to find latest version for $packageName")
