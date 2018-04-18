package com.linkedin.pygradle.pypi.internal.model

import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.PythonPackageVersion
import com.linkedin.pygradle.pypi.model.extra.DependencyCondition

/**
 * Dependency Implementation
 */
internal data class EditableDependency(override val name: String,
                              override val version: PythonPackageVersion,
                              override val operator: DependencyOperator,
                              override val extras: MutableSet<DependencyCondition>) : Dependency
