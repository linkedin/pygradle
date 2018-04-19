package com.linkedin.pygradle.pypi.exception

/**
 * There was no compatible dependency found
 */
class NoCompatibleDependencyException: RuntimeException("Unable to find any compatible dependencies")
