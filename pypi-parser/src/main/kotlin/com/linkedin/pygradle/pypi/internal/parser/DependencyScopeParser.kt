package com.linkedin.pygradle.pypi.internal.parser

import com.linkedin.pygradle.pypi.model.extra.DependencyCondition
import com.linkedin.pygradle.pypi.model.extra.PackageRequiredDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition

internal object DependencyScopeParser {
    private val scopeParser = Regex("(?<name>[:a-zA-Z0-9_\\.]+)((?<operator>[=><!]{1,2})(?<option>.+))?")

    internal fun matches(input: String) = scopeParser.matches(input)

    /**
     * Parses a dependency scope and returns a collection of conditions
     */
    @JvmStatic
    fun parseScope(input: String): Collection<DependencyCondition> {
        val result = scopeParser.find(input) ?: return emptyList()
        val groups = result.groups
        val operatorString = groups["operator"]?.value
        val optionString = groups["option"]?.value
        val name = groups["name"]?.value ?: throw IllegalArgumentException("Regex Match failed.")

        return if (operatorString == null || optionString == null) {
            listOf(PackageRequiredDependencyCondition(name))
        } else {
            // remove prefix
            val requirement = if (name.lastIndexOf(":") != -1) {
                name.substring(name.lastIndexOf(":") + 1)
            } else {
                name
            }
            val condition = DependencyOperatorParser.parseComparison(operatorString)
                ?: throw IllegalArgumentException("operator string was null")
            val removedQuotes = optionString.replace("\"", "").replace("'", "")
            listOf(SystemDependencyCondition(requirement, removedQuotes, condition))
        }
    }
}
