/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.util.values.PyGradleTask
import spock.lang.Shared
import spock.lang.Unroll

import static com.linkedin.gradle.python.util.values.PyGradleTask.*

/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 *
 * The data I am comparing against is version 0.4.1
 */
@Unroll
class TaskInventoryTest extends AbstractPluginIntegrationSpec {
    @Shared
        pygradleTasksMap = [
            CLEAN_SAVE_VENV    :
                """|:cleanSaveVenv
                    |No task dependencies""".stripMargin().stripIndent(),
            SETUP_PY_WRITER    :
                """|:generateSetupPy
                    |No task dependencies""".stripMargin().stripIndent(),
            VENV_CREATE        :
                """|:createVirtualEnvironment
                    |\\--- :pinRequirements""".stripMargin().stripIndent(),
            INSTALL_BUILD_REQS :
                """|:installBuildRequirements
                    |\\--- :installSetupRequirements
                    |5\\--- :installLinks
                    |55\\--- :createVirtualEnvironment
                    |555\\--- :pinRequirements""".stripMargin().stripIndent(),
            SETUP_LINKS        :
                """|:installLinks
                    |\\--- :createVirtualEnvironment
                    |5\\--- :pinRequirements""".stripMargin().stripIndent(),
            INSTALL_PROJECT    :
                """|:installProject
                    |\\--- :installTestRequirements
                    |5\\--- :installPythonRequirements
                    |55\\--- :installBuildRequirements
                    |555\\--- :installSetupRequirements
                    |5555\\--- :installLinks
                    |55555\\--- :createVirtualEnvironment
                    |555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            INSTALL_PYTHON_REQS:
                """|:installPythonRequirements
                    |\\--- :installBuildRequirements
                    |5\\--- :installSetupRequirements
                    |55\\--- :installLinks
                    |555\\--- :createVirtualEnvironment
                    |5555\\--- :pinRequirements""".stripMargin().stripIndent(),
            INSTALL_SETUP_REQS :
                """|:installSetupRequirements
                    |\\--- :installLinks
                    |5\\--- :createVirtualEnvironment
                    |55\\--- :pinRequirements""".stripMargin().stripIndent(),
            INSTALL_TEST_REQS  :
                """|:installTestRequirements
                    |\\--- :installPythonRequirements
                    |5\\--- :installBuildRequirements
                    |55\\--- :installSetupRequirements
                    |555\\--- :installLinks
                    |5555\\--- :createVirtualEnvironment
                    |55555\\--- :pinRequirements""".stripMargin().stripIndent(),
            PIN_REQUIREMENTS   :
                """|:pinRequirements
                    |No task dependencies""".stripMargin().stripIndent(),
            BUILD_DOCS_HTML    :
                """|:buildDocsHtml
                    |+--- :installBuildRequirements
                    ||4\\--- :installSetupRequirements
                    ||54\\--- :installLinks
                    ||554\\--- :createVirtualEnvironment
                    ||5554\\--- :pinRequirements
                    |\\--- :installProject
                    |5\\--- :installTestRequirements
                    |55\\--- :installPythonRequirements
                    |555\\--- :installBuildRequirements
                    |5555\\--- :installSetupRequirements
                    |55555\\--- :installLinks
                    |555555\\--- :createVirtualEnvironment
                    |5555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            BUILD_DOCS_JSON    :
                """|:buildDocsJson
                    |+--- :installBuildRequirements
                    ||4\\--- :installSetupRequirements
                    ||54\\--- :installLinks
                    ||554\\--- :createVirtualEnvironment
                    ||5554\\--- :pinRequirements
                    |\\--- :installProject
                    |5\\--- :installTestRequirements
                    |55\\--- :installPythonRequirements
                    |555\\--- :installBuildRequirements
                    |5555\\--- :installSetupRequirements
                    |55555\\--- :installLinks
                    |555555\\--- :createVirtualEnvironment
                    |5555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            BUILD_DOCS:
                """|:buildDocs
                    |+--- :buildDocsHtml
                    ||4+--- :installBuildRequirements
                    ||4|4\\--- :installSetupRequirements
                    ||4|54\\--- :installLinks
                    ||4|554\\--- :createVirtualEnvironment
                    ||4|5554\\--- :pinRequirements
                    ||4\\--- :installProject
                    ||54\\--- :installTestRequirements
                    ||554\\--- :installPythonRequirements
                    ||5554\\--- :installBuildRequirements
                    ||55554\\--- :installSetupRequirements
                    ||555554\\--- :installLinks
                    ||5555554\\--- :createVirtualEnvironment
                    ||55555554\\--- :pinRequirements
                    |\\--- :buildDocsJson
                    |5+--- :installBuildRequirements
                    |5|4\\--- :installSetupRequirements
                    |5|54\\--- :installLinks
                    |5|554\\--- :createVirtualEnvironment
                    |5|5554\\--- :pinRequirements
                    |5\\--- :installProject
                    |55\\--- :installTestRequirements
                    |555\\--- :installPythonRequirements
                    |5555\\--- :installBuildRequirements
                    |55555\\--- :installSetupRequirements
                    |555555\\--- :installLinks
                    |5555555\\--- :createVirtualEnvironment
                    |55555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            COVERAGE           :
                """|:coverage
                    |+--- :installBuildRequirements
                    ||4\\--- :installSetupRequirements
                    ||54\\--- :installLinks
                    ||554\\--- :createVirtualEnvironment
                    ||5554\\--- :pinRequirements
                    |\\--- :installProject
                    |5\\--- :installTestRequirements
                    |55\\--- :installPythonRequirements
                    |555\\--- :installBuildRequirements
                    |5555\\--- :installSetupRequirements
                    |55555\\--- :installLinks
                    |555555\\--- :createVirtualEnvironment
                    |5555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            CHECKSTYLE         :
                """|:flake8Checkstyle
                    |\\--- :installBuildRequirements
                    |5\\--- :installSetupRequirements
                    |55\\--- :installLinks
                    |555\\--- :createVirtualEnvironment
                    |5555\\--- :pinRequirements""".stripMargin().stripIndent(),
            PACKAGE_DOCS       :
                """|:packageDocs
                    |\\--- :buildDocs
                    |5+--- :buildDocsHtml
                    |5|4+--- :installBuildRequirements
                    |5|4|4\\--- :installSetupRequirements
                    |5|4|54\\--- :installLinks
                    |5|4|554\\--- :createVirtualEnvironment
                    |5|4|5554\\--- :pinRequirements
                    |5|4\\--- :installProject
                    |5|54\\--- :installTestRequirements
                    |5|554\\--- :installPythonRequirements
                    |5|5554\\--- :installBuildRequirements
                    |5|55554\\--- :installSetupRequirements
                    |5|555554\\--- :installLinks
                    |5|5555554\\--- :createVirtualEnvironment
                    |5|55555554\\--- :pinRequirements
                    |5\\--- :buildDocsJson
                    |55+--- :installBuildRequirements
                    |55|4\\--- :installSetupRequirements
                    |55|54\\--- :installLinks
                    |55|554\\--- :createVirtualEnvironment
                    |55|5554\\--- :pinRequirements
                    |55\\--- :installProject
                    |555\\--- :installTestRequirements
                    |5555\\--- :installPythonRequirements
                    |55555\\--- :installBuildRequirements
                    |555555\\--- :installSetupRequirements
                    |5555555\\--- :installLinks
                    |55555555\\--- :createVirtualEnvironment
                    |555555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            PACKAGE_JSON_DOCS  :
                """|:packageJsonDocs
                    |\\--- :buildDocs
                    |5+--- :buildDocsHtml
                    |5|4+--- :installBuildRequirements
                    |5|4|4\\--- :installSetupRequirements
                    |5|4|54\\--- :installLinks
                    |5|4|554\\--- :createVirtualEnvironment
                    |5|4|5554\\--- :pinRequirements
                    |5|4\\--- :installProject
                    |5|54\\--- :installTestRequirements
                    |5|554\\--- :installPythonRequirements
                    |5|5554\\--- :installBuildRequirements
                    |5|55554\\--- :installSetupRequirements
                    |5|555554\\--- :installLinks
                    |5|5555554\\--- :createVirtualEnvironment
                    |5|55555554\\--- :pinRequirements
                    |5\\--- :buildDocsJson
                    |55+--- :installBuildRequirements
                    |55|4\\--- :installSetupRequirements
                    |55|54\\--- :installLinks
                    |55|554\\--- :createVirtualEnvironment
                    |55|5554\\--- :pinRequirements
                    |55\\--- :installProject
                    |555\\--- :installTestRequirements
                    |5555\\--- :installPythonRequirements
                    |55555\\--- :installBuildRequirements
                    |555555\\--- :installSetupRequirements
                    |5555555\\--- :installLinks
                    |55555555\\--- :createVirtualEnvironment
                    |555555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            FLAKE              :
                """|:flake8
                    |\\--- :installBuildRequirements
                    |5\\--- :installSetupRequirements
                    |55\\--- :installLinks
                    |555\\--- :createVirtualEnvironment
                    |5555\\--- :pinRequirements""".stripMargin().stripIndent(),
            CHECK              :
                """|:check
                    |+--- :flake8
                    ||4\\--- :installBuildRequirements
                    ||54\\--- :installSetupRequirements
                    ||554\\--- :installLinks
                    ||5554\\--- :createVirtualEnvironment
                    ||55554\\--- :pinRequirements
                    |\\--- :pytest
                    |5+--- :installBuildRequirements
                    |5|4\\--- :installSetupRequirements
                    |5|54\\--- :installLinks
                    |5|554\\--- :createVirtualEnvironment
                    |5|5554\\--- :pinRequirements
                    |5\\--- :installProject
                    |55\\--- :installTestRequirements
                    |555\\--- :installPythonRequirements
                    |5555\\--- :installBuildRequirements
                    |55555\\--- :installSetupRequirements
                    |555555\\--- :installLinks
                    |5555555\\--- :createVirtualEnvironment
                    |55555555\\--- :pinRequirements""".stripMargin().stripIndent(),
            PYTEST             :
                """|:pytest
                    |+--- :installBuildRequirements
                    ||4\\--- :installSetupRequirements
                    ||54\\--- :installLinks
                    ||554\\--- :createVirtualEnvironment
                    ||5554\\--- :pinRequirements
                    |\\--- :installProject
                    |5\\--- :installTestRequirements
                    |55\\--- :installPythonRequirements
                    |555\\--- :installBuildRequirements
                    |5555\\--- :installSetupRequirements
                    |55555\\--- :installLinks
                    |555555\\--- :createVirtualEnvironment
                    |5555555\\--- :pinRequirements""".stripMargin().stripIndent()
        ]

    def pattern = ~/(?ms)(?<=------------------------------------------------------------\nRoot project\n------------------------------------------------------------\n\n)(.*?)(?=\n\nTo see task dependency tree for a specific task)/

    @Shared
    pexTasksMap

    @Shared
    cliTasksMap

    @Shared
    webTasksMap

    @Shared
    flyerTasks

    @Shared
    sdistTasks

    static final String TASKTREE = "tasktree"

    def setup() {

        pexTasksMap = pygradleTasksMap.clone()
        pexTasksMap.put(BUILD_WHEELS,
            """|:buildWheels
                |\\--- :installProject
                |5\\--- :installTestRequirements
                |55\\--- :installPythonRequirements
                |555\\--- :installBuildRequirements
                |5555\\--- :installSetupRequirements
                |55555\\--- :installLinks
                |555555\\--- :createVirtualEnvironment
                |5555555\\--- :pinRequirements""".stripMargin().stripIndent())
        pexTasksMap.put(BUILD_PEX,
            """|:buildPex
                |\\--- :buildWheels
                |5\\--- :installProject
                |55\\--- :installTestRequirements
                |555\\--- :installPythonRequirements
                |5555\\--- :installBuildRequirements
                |55555\\--- :installSetupRequirements
                |555555\\--- :installLinks
                |5555555\\--- :createVirtualEnvironment
                |55555555\\--- :pinRequirements""".stripMargin().stripIndent())
        pexTasksMap.put(PACKAGE_DEPLOYABLE,
            """|:packageDeployable
                |\\--- :buildPex
                |5\\--- :buildWheels
                |55\\--- :installProject
                |555\\--- :installTestRequirements
                |5555\\--- :installPythonRequirements
                |55555\\--- :installBuildRequirements
                |555555\\--- :installSetupRequirements
                |5555555\\--- :installLinks
                |55555555\\--- :createVirtualEnvironment
                |555555555\\--- :pinRequirements""".stripMargin().stripIndent())


        cliTasksMap = pexTasksMap.clone()
        cliTasksMap.put(GENERATE_COMPLETIONS,
            """|:generateCompletions
                |+--- :installBuildRequirements
                ||4\\--- :installSetupRequirements
                ||54\\--- :installLinks
                ||554\\--- :createVirtualEnvironment
                ||5554\\--- :pinRequirements
                |\\--- :installProject
                |5\\--- :installTestRequirements
                |55\\--- :installPythonRequirements
                |555\\--- :installBuildRequirements
                |5555\\--- :installSetupRequirements
                |55555\\--- :installLinks
                |555555\\--- :createVirtualEnvironment
                |5555555\\--- :pinRequirements""".stripMargin().stripIndent())

        webTasksMap = pexTasksMap.clone()
        webTasksMap.put(BUILD_WEB_APPLICATION,
            """|:buildWebApplication
                |\\--- :buildPex
                |5\\--- :buildWheels
                |55\\--- :installProject
                |555\\--- :installTestRequirements
                |5555\\--- :installPythonRequirements
                |55555\\--- :installBuildRequirements
                |555555\\--- :installSetupRequirements
                |5555555\\--- :installLinks
                |55555555\\--- :createVirtualEnvironment
                |555555555\\--- :pinRequirements""".stripMargin().stripIndent())
        webTasksMap.put(PACKAGE_WEB_APPLICATION,
            """|:packageWebApplication
                |\\--- :buildWebApplication
                |5\\--- :buildPex
                |55\\--- :buildWheels
                |555\\--- :installProject
                |5555\\--- :installTestRequirements
                |55555\\--- :installPythonRequirements
                |555555\\--- :installBuildRequirements
                |5555555\\--- :installSetupRequirements
                |55555555\\--- :installLinks
                |555555555\\--- :createVirtualEnvironment
                |5555555555\\--- :pinRequirements""".stripMargin().stripIndent())



        flyerTasks = webTasksMap.clone()
        flyerTasks.put(SETUP_RESOURCE_LINK,
        """|:setupResourceLink
           |No task dependencies""")

        flyerTasks.put(PACKAGE_RESOURCE_FILES,
            """|:packageResourceFiles
               |\\--- :buildWebApplication
               |5\\--- :buildPex
               |55\\--- :buildWheels
               |555\\--- :installProject
               |5555+--- :installTestRequirements
               |5555|4\\--- :installPythonRequirements
               |5555|54\\--- :installBuildRequirements
               |5555|554\\--- :installSetupRequirements
               |5555|5554\\--- :installLinks
               |5555|55554\\--- :createVirtualEnvironment
               |5555|555554\\--- :pinRequirements
               |5555\\--- :setupResourceLink""".stripMargin().stripIndent())

        sdistTasks = pygradleTasksMap.clone()
        sdistTasks.put(PACKAGE_SDIST,
        """|:packageSdist
           |+--- :installBuildRequirements
           ||4\\--- :installSetupRequirements
           ||54\\--- :installLinks
           ||554\\--- :createVirtualEnvironment
           ||5554\\--- :pinRequirements
           |\\--- :installProject
           |5\\--- :installTestRequirements
           |55\\--- :installPythonRequirements
           |555\\--- :installBuildRequirements
           |5555\\--- :installSetupRequirements
           |55555\\--- :installLinks
           |555555\\--- :createVirtualEnvironment
           |5555555\\--- :pinRequirements""".stripMargin().stripIndent())

        buildFile << """ plugins { id "com.dorongold.task-tree" version "1.2.2" } """
    }

    def parseResponse(String responseText) {
        def returnText = responseText.find(pattern)
        returnText = returnText.replaceAll(/ {5}/, "5")
        returnText.replaceAll(/ {4}/, "4").trim()
    }

    def "com.linkedin.python:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << pygradleTasksMap.keySet()
        runExpected << pygradleTasksMap.values()
    }

    def "com.linkedin.python-pex:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE, '--stacktrace'])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << pexTasksMap.keySet()
        runExpected << pexTasksMap.values()
    }

    def "com.linkedin.python-cli:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-cli'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << cliTasksMap.keySet()
        runExpected << cliTasksMap.values()
    }

    def "com.linkedin.python-web-app:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-web-app'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << webTasksMap.keySet()
        runExpected << webTasksMap.values()
    }

    def "com.linkedin.python-flyer:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-flyer'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << flyerTasks.keySet()
        runExpected << flyerTasks.values()
    }

    def "com.linkedin.python-sdist:#runTask"(PyGradleTask runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize() == runExpected.normalize()

        where:
        runTask << sdistTasks.keySet()
        runExpected << sdistTasks.values()
    }


}
