package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.exception.PyPiParserBugException
import com.linkedin.pygradle.pypi.internal.extractField
import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.internal.model.EditableDependency
import com.linkedin.pygradle.pypi.internal.parser.DependencyOperatorParser
import com.linkedin.pygradle.pypi.internal.parser.DependencyScopeParser
import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DefaultDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.DependencyCondition
import com.linkedin.pygradle.pypi.service.PyPiRemote

internal class RequiresTxtParser(private val remote: PyPiRemote) : AbstractDependencyParser() {

    private val dependencyParser = Regex("(?<name>[a-zA-Z0-9_.]+)(?<options>.+)?")

    override fun calculateDependencies(requires: String): List<Dependency> {
        val defaultDependencyCondition = DefaultDependencyCondition()

        val dependenciesFrom = mutableMapOf<DependencyCondition, MutableList<DependencyComponent>>(Pair(defaultDependencyCondition, mutableListOf()))
        var scope: Collection<DependencyCondition> = listOf(defaultDependencyCondition)
        for (line in requires.lines()) {
            if (line.startsWith("[") && line.endsWith("]") && DependencyScopeParser.matches(line.substring(1, line.length - 1))) {
                scope = DependencyScopeParser.parseScope(line.substring(1, line.length - 1))
                scope.filter { !dependenciesFrom.keys.contains(it) }.forEach { dependenciesFrom[it] = mutableListOf() }

            } else if (dependencyParser.matches(line)) {
                val dependency = breakIntoComponents(line)
                scope.forEach { dependenciesFrom.getOrElse(it, { throw PyPiParserBugException("Unknown scope!")}).addAll(dependency) }
            }
        }

        val dependencyList = mutableListOf<EditableDependency>()

        dependenciesFrom.forEach { key, value ->
            value.forEach { dependency ->
                val packageDetails = remote.resolvePackage(dependency.name)
                var foundDep = dependencyList.find { packageDetails.getPackageName()== it.name && dependency.version.version == it.version.toVersionString() }
                if (foundDep == null) {
                    foundDep = EditableDependency(packageDetails.getPackageName(), DefaultPythonPackageVersion(dependency.version.version),
                        dependency.version.comparison, mutableSetOf())
                    dependencyList.add(foundDep)
                }

                foundDep.extras.add(key)

            }
        }

        return dependencyList
    }

    private fun breakIntoComponents(line: String): List<DependencyComponent> {
        val matches = dependencyParser.find(line) ?: throw PyPiParserBugException("Regex parse failed")
        val name = matches.extractField("name")
        val options = matches.groups["options"]?.value

        val resolvePackage = remote.resolvePackage(name)

        val versionOptions = if (options != null) {
            options.split(",").map {
                val versionStartsAt = it.indexOfFirst { it.isLetterOrDigit() }
                val comp = it.substring(0, versionStartsAt)
                val version = it.substring(versionStartsAt)
                val parseComparison = DependencyOperatorParser.getParsedComparison(comp)
                DependencyVersionElement(parseComparison, version)
            }
        } else {
            listOf(DependencyVersionElement(DependencyOperator.GREATER_THAN_EQUAL, resolvePackage.getLatestVersion().toVersionString()))
        }

        // Use the name from PyPi as it's probably more correct than random user input
        return versionOptions.map { DependencyComponent(resolvePackage.getPackageName(), it) }
    }
}
