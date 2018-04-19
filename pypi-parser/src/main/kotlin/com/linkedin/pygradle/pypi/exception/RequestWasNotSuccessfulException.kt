package com.linkedin.pygradle.pypi.exception

/**
 * Network request for a package failed
 */
class RequestWasNotSuccessfulException(packageName: String) : RuntimeException("Unable to request details for $packageName")
