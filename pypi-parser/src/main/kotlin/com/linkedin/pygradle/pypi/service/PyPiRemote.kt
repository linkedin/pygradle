package com.linkedin.pygradle.pypi.service

interface PyPiRemote {
    fun resolvePackage(name: String): PyPiPackageDetails
}
