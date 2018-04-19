package com.linkedin.pygradle.pypi.exception

class NoVersionAvailableException(packageName: String) : RuntimeException("Unable to find latest version for $packageName")
