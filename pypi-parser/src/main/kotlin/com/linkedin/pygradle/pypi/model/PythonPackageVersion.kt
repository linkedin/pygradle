package com.linkedin.pygradle.pypi.model

interface PythonPackageVersion {
    fun toVersionString(): String

    fun isWildcardVersion(): Boolean

    fun getRelease(): String
    fun getPost(): String?
    fun getDev(): String?
    fun getLocal(): String?
    fun getEpoch(): String?

    fun getGroups(): List<String?>
}
