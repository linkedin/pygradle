package com.linkedin.pygradle.pypi.model

interface DependencyOptions {
    /**
     * Python, GCC, etc
     */
    fun getSystemVersion(name: String): String?

    /**
     * If the package exists
     */
    fun hasPackage(name: String): Boolean
}
