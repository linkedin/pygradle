package com.linkedin.pygradle.pypi.model

/**
 * A object used to get options to decide if a dependency should be included.
 */
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
