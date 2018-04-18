package com.linkedin.pygradle.pypi.model.extra

import com.linkedin.pygradle.pypi.internal.model.VersionExpressionEvaluator
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.DependencyOptions
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * A dependency on something like Python
 */
data class SystemDependencyCondition(val name: String, val version: String, val condition: DependencyOperator) : DependencyCondition {

    override fun toString(): String = "${name}_${condition.description}_$version"

    override fun getExpression(): String = toString()

    override fun shouldInclude(options: DependencyOptions): Boolean {
        val systemVersion = options.getSystemVersion(name) ?: return false
        return VersionExpressionEvaluator.isExpressionTrue(version, condition, systemVersion)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is SystemDependencyCondition) {
            EqualsBuilder()
                .append(other.name, name)
                .append(other.version, version)
                .append(other.condition, condition)
                .isEquals
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(name)
            .append(version)
            .append(condition)
            .append(getExpression())
            .toHashCode()
    }
}
