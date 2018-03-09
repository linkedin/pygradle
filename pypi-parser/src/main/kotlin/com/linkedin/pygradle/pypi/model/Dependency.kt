package com.linkedin.pygradle.pypi.model

import com.linkedin.pygradle.pypi.model.extra.DependencyCondition

interface Dependency {
    val name: String
    val version: PythonPackageVersion
    val operator: DependencyOperator
    val extras: Collection<DependencyCondition>
}
