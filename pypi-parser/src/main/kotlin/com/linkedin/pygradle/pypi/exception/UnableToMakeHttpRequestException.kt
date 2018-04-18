package com.linkedin.pygradle.pypi.exception

class UnableToMakeHttpRequestException(url: String) : RuntimeException("Unable to make request to/for $url")
