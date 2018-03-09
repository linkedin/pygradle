package com.linkedin.pygradle.pypi.model.extra

import com.linkedin.pygradle.pypi.model.DependencyOptions
import org.apache.commons.lang3.builder.HashCodeBuilder

class DefaultDependencyCondition : DependencyCondition {
    override fun shouldInclude(options: DependencyOptions) = true

    override fun getExpression(): String = "always_use"

    override fun equals(other: Any?): Boolean {
        return other is DefaultDependencyCondition
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(getExpression()).toHashCode()
    }

    override fun toString(): String = getExpression()
}
