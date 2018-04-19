package com.linkedin.pygradle.pypi.exception

class InternalBugException(message: String) : RuntimeException("Pivy Internal Error: $message")
