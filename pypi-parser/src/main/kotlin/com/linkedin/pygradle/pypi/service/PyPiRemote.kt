package com.linkedin.pygradle.pypi.service

/**
 * Interface to interact with PyPi
 */
interface PyPiRemote {
    /**
     * Resolve the details for a given package
     */
    fun resolvePackage(name: String): PyPiPackageDetails
}
