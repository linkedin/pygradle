package com.linkedin.pygradle.pypi.internal.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.readValue
import com.linkedin.pygradle.pypi.internal.ObjectMapperContainer
import com.linkedin.pygradle.pypi.internal.model.DefaultPythonPackageVersion
import com.linkedin.pygradle.pypi.internal.model.EditableDependency
import com.linkedin.pygradle.pypi.internal.parser.DependencyOperatorParser
import com.linkedin.pygradle.pypi.internal.parser.DependencyScopeParser
import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DefaultDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.DependencyCondition
import com.linkedin.pygradle.pypi.service.PyPiRemote
import org.slf4j.LoggerFactory

class WheelManifestParser(private val remote: PyPiRemote) : AbstractDependencyParser() {

    private val om = ObjectMapperContainer.objectMapper
    private val log = LoggerFactory.getLogger(WheelManifestParser::class.java)

    override fun calculateDependencies(requires: String): List<Dependency> {
        val dependencyList = mutableListOf<EditableDependency>()

        val parsedRequires = try {
            om.readValue<Map<String, Any>>(requires)
        } catch (e: JsonMappingException) {
            log.error("Unable to parse `{}`", requires)
            throw RuntimeException(e)
        }

        val dependencies: List<RequiresDependency> =
            if (parsedRequires["run_requires"] != null) {
                om.convertValue(parsedRequires["run_requires"], object : TypeReference<List<RequiresDependency>>() {})
            } else {
                emptyList()
            }

        val defaultScope = listOf(DefaultDependencyCondition())

        dependencies.forEach { dep ->
            val scope = if (dep.environment != null) DependencyScopeParser.parseScope(dep.environment) else defaultScope
            dep.requires.forEach {
                if (v1Regex.matches(it)) {
                    val matchResult = v1Regex.find(it)!!
                    val name = matchResult.groups["dep"]!!.value
                    val versions = matchResult.groups["versions"]!!.value.split(",")

                    versions.forEach {
                        val startOfVersion = it.indexOfFirst { it.isLetterOrDigit() }
                        val operator = DependencyOperatorParser.parseComparison(it.substring(0, startOfVersion))!!
                        includeDependency(dependencyList, name, it.substring(startOfVersion), operator, scope)
                    }
                } else if (v2Regex.matches(it)) {
                    val matchResult = v2Regex.find(it)!!
                    val name = matchResult.groups["dep"]!!.value
                    val versions = matchResult.groups["versions"]!!.value.split(",")
                        .map { it.replace(" ", "").trim() }

                    versions.forEach {
                        val startOfVersion = it.indexOfFirst { it.isLetterOrDigit() }
                        val operator = DependencyOperatorParser.parseComparison(it.substring(0, startOfVersion))!!
                        includeDependency(dependencyList, name, it.substring(startOfVersion), operator, scope)
                    }
                } else if (v3Regex.matches(it)) {
                    val matchResult = v3Regex.find(it)!!
                    val name = matchResult.groups["dep"]!!.value

                    val versionString = remote.resolvePackage(name).getLatestVersion()!!.toVersionString()

                    includeDependency(dependencyList, name, versionString, DependencyOperator.GREATER_THAN_EQUAL, scope)
                } else {
                    throw RuntimeException("Unable to parse wheel format ($it)")
                }
            }
        }

        return dependencyList
    }

    private fun includeDependency(dependencyList: MutableList<EditableDependency>, name: String, version: String,
                                  operator: DependencyOperator, scope: Collection<DependencyCondition>) {
        val packageDetails = remote.resolvePackage(name)
        var foundDep = dependencyList.find { it.name == packageDetails.getPackageName() && version == it.version.toVersionString() && it.operator == operator }
        if (foundDep == null) {
            foundDep = EditableDependency(packageDetails.getPackageName(), DefaultPythonPackageVersion(version), operator, mutableSetOf())
            dependencyList.add(foundDep)
        }

        foundDep.extras.addAll(scope)
    }

    class RequiresDependency(val environment: String?,
                             val requires: List<String>)

    companion object {
        private const val DEPENDENCY_LINE_V1 = "(?<dep>[a-zA-Z._\\-\\d]+) \\((?<versions>.*)\\)"
        private val v1Regex = Regex(DEPENDENCY_LINE_V1)

        private const val DEPENDENCY_LINE_V2 = "(?<dep>[a-zA-Z._\\-\\d]+) (?<versions>.*)"
        private val v2Regex = Regex(DEPENDENCY_LINE_V2)

        private const val DEPENDENCY_LINE_V3 = "(?<dep>[a-zA-Z._\\-\\d]+)"
        private val v3Regex = Regex(DEPENDENCY_LINE_V3)
    }
}
