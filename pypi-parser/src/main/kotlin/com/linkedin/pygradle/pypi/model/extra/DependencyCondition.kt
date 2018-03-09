package com.linkedin.pygradle.pypi.model.extra

import com.linkedin.pygradle.pypi.model.DependencyOptions

interface DependencyCondition {
    fun shouldInclude(options: DependencyOptions): Boolean

    fun getExpression(): String
}

