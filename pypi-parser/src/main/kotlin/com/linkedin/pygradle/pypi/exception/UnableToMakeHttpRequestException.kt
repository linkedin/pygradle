package com.linkedin.pygradle.pypi.exception

/**
 * Network request to a URL failed.
 */
class UnableToMakeHttpRequestException(url: String) : RuntimeException("Unable to make request to/for $url")
