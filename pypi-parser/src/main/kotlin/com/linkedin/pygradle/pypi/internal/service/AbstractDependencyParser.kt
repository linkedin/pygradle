package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator

internal abstract class AbstractDependencyParser {
    abstract fun calculateDependencies(requires: String): List<Dependency>
    data class DependencyComponent(val name: String, val version: DependencyVersionElement)
    data class DependencyVersionElement(val comparison: DependencyOperator, val version: String)
}
