package com.linkedin.pygradle.pypi.model

import com.linkedin.pygradle.pypi.model.extra.DependencyCondition

/**
 * Marker interface for a Dependency
 */
interface Dependency {
    /**
     * The name
     */
    val name: String

    /**
     * The version
     */
    val version: PythonPackageVersion

    /**
     * The operator for why it's being included
     */
    val operator: DependencyOperator

    /**
     * Any extra requirements
     */
    val extras: Collection<DependencyCondition>
}
