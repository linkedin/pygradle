package com.linkedin.pygradle.pypi.exception

class RequestWasNotSuccessfulException(packageName: String) : RuntimeException("Unable to request details for $packageName")
