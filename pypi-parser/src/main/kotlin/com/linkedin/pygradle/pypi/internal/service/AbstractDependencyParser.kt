package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator

internal abstract class AbstractDependencyParser {
    /**
     * Given a requirement file, generate a dependency list
     */
    abstract fun calculateDependencies(requires: String): List<Dependency>
    internal data class DependencyComponent(val name: String, val version: DependencyVersionElement)
    internal data class DependencyVersionElement(val comparison: DependencyOperator, val version: String)
}
