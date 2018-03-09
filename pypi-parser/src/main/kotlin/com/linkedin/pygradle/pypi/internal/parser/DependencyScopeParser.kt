package com.linkedin.pygradle.pypi.internal.parser

import com.linkedin.pygradle.pypi.model.extra.DependencyCondition
import com.linkedin.pygradle.pypi.model.extra.PackageRequiredDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition

object DependencyScopeParser {
    private val scopeParser = Regex("(?<name>[:a-zA-Z0-9_\\.]+)((?<operator>[=><!]{1,2})(?<option>.+))?")

    @JvmStatic
    fun matches(input: String) = scopeParser.matches(input)

    @JvmStatic
    fun parseScope(input: String): Collection<DependencyCondition> {
        val groups = scopeParser.find(input)!!.groups
        val operatorString = if (groups["operator"] != null) groups["operator"]!!.value else null
        val optionString = if (groups["option"] != null) groups["option"]!!.value else null
        val name = groups["name"]!!.value

        return if (operatorString == null || optionString == null) {
            listOf(PackageRequiredDependencyCondition(name))
        } else {
            // remove prefix
            val requirement = if (name.lastIndexOf(":") != -1) {
                name.substring(name.lastIndexOf(":") + 1)
            } else {
                name
            }
            val condition = DependencyOperatorParser.parseComparison(operatorString)!!
            val removedQuotes = optionString.replace("\"", "").replace("'", "")
            listOf(SystemDependencyCondition(requirement, removedQuotes, condition))
        }
    }
}
