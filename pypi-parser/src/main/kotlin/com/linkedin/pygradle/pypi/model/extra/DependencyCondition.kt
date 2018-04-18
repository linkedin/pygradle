package com.linkedin.pygradle.pypi.model.extra

import com.linkedin.pygradle.pypi.model.DependencyOptions

/**
 * Allows for dependencies to be conditional
 */
interface DependencyCondition {
    /**
     * When true, it should be included in the graph.
     */
    fun shouldInclude(options: DependencyOptions): Boolean

    /**
     * The expression in the file
     */
    fun getExpression(): String
}

