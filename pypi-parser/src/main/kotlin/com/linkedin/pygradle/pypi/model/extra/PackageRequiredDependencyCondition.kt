package com.linkedin.pygradle.pypi.model.extra

import com.linkedin.pygradle.pypi.model.DependencyOptions
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Useful when you require a package
 */
data class PackageRequiredDependencyCondition(val name: String) : DependencyCondition {
    override fun shouldInclude(options: DependencyOptions) = options.hasPackage(name)

    override fun getExpression(): String = "depends_on_$name"

    override fun equals(other: Any?): Boolean {
        return if (other is PackageRequiredDependencyCondition) {
            EqualsBuilder()
                .append(other.name, name)
                .isEquals
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(getExpression()).toHashCode()
    }

    override fun toString(): String = getExpression()
}
