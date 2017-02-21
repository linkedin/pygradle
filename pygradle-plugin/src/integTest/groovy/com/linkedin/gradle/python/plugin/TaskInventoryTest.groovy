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

import com.linkedin.gradle.python.util.StandardTextValuesTasks
import spock.lang.Shared
import spock.lang.Unroll

import static com.linkedin.gradle.python.util.StandardTextValuesTasks.*

/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 *
 * The data I am comparing against is version 0.4.1
 */
class TaskInventoryTest extends AbstractPluginIntegrationSpec {
    @Shared
    def pygradleTasksMap = [
        SETUP_PY_WRITER:
        """|:generateSetupPy
           |No task dependencies""".stripMargin().stripIndent(),
        VENV_CREATE:
        """|:createVirtualEnvironment
           |\\--- :pinRequirements""".stripMargin().stripIndent(),
        INSTALL_BUILD_REQS:
        """|:installBuildRequirements
           |\\--- :installSetupRequirements
           |5\\--- :installLinks
           |55\\--- :createVirtualEnvironment
           |555\\--- :pinRequirements""".stripMargin().stripIndent(),
        SETUP_LINKS:
        """|:installLinks
           |\\--- :createVirtualEnvironment
           |5\\--- :pinRequirements""".stripMargin().stripIndent(),
        BUILD_DOCS_HTML:
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
           |5555555\\--- :pinRequirements""".stripMargin().stripIndent()]

    def pattern = ~/(?ms)(?<=------------------------------------------------------------\nRoot project\n------------------------------------------------------------\n\n)(.*?)(?=\n\nTo see task dependency tree for a specific task)/
    def eol = System.getProperty("line.separator")

    def pygradleTasks
    def pexTasks
    def cliTasks
    def webTasks
    def flyerTasks
    def sdistTasks
    public static final String TASKTREE = "tasktree"

    def setup() {
        pygradleTasks = [CLEAN_SAVE_VENV,
                         INSTALL_PROJECT,
                         INSTALL_PYTHON_REQS,
                         INSTALL_SETUP_REQS,
                         INSTALL_TEST_REQS,
                         PIN_REQUIREMENTS,
                         BUILD_DOCS_JSON,
                         COVERAGE,
                         CHECKSTYLE,
                         PACKAGE_DOCS,
                         PACKAGE_JSON_DOCS,
                         FLAKE,
                         CHECK,
                         PYTEST
        ]




        pexTasks = pygradleTasks.clone()
        pexTasks.addAll([BUILD_WHEELS,
                         BUILD_PEX,
                         PACKAGE_DEPLOYABLE])

        cliTasks = pexTasks.clone()
        cliTasks.add(GENERATE_COMPLETIONS)

        webTasks = pexTasks.clone()
        webTasks.addAll([BUILD_WEB_APPLICATION,
                         PACKAGE_WEB_APPLICATION])

        flyerTasks = webTasks.clone()
        flyerTasks.addAll([SETUP_RESOURCE_LINK,
                           PACKAGE_RESOURCE_FILES])

        sdistTasks = pygradleTasks.clone()
        sdistTasks.add(PACKAGE_SDIST)

        buildFile << """ plugins { id "com.dorongold.task-tree" version "1.2.2" } """
    }

    def parseResponse (String responseText){
        def returnText = responseText.find(pattern)
        returnText = returnText.replaceAll(/ {5}/, "5")
        returnText.replaceAll(/ {4}/, "4").trim()
    }

    @Unroll
    def "com.linkedin.python:#runTask"(StandardTextValuesTasks runTask, String runExpected) {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(runTask, [TASKTREE])

        then: "make sure tasks are present"
        parseResponse(result.output).normalize()== runExpected.normalize()

        where:
        runTask << pygradleTasksMap.keySet()
        runExpected << pygradleTasksMap.values()
    }

    def "verify com.linkedin.python-pex task list"() {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(TASKS, ["--all"])

        then: "make sure tasks are present"
        pexTasks.each { StandardTextValuesTasks it ->
            assert result.output.contains(it.value)
        }
    }

    def "verify com.linkedin.python-cli task list"() {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-cli'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(TASKS, ["--all"])

        then: "make sure it passes initially first"
        cliTasks.each { StandardTextValuesTasks it ->
            assert result.output.contains(it.value)
        }
    }

    def "verify com.linkedin.python-web-app task list"() {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-web-app'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(TASKS, ["--all"])

        then: "make sure it passes initially first"
        webTasks.each { StandardTextValuesTasks it ->
            assert result.output.contains(it.value)
        }
    }

    def "verify com.linkedin.python-flyer task list"() {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-flyer'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(TASKS, ["--all"])

        then: "make sure it passes initially first"
        webTasks.each { StandardTextValuesTasks it ->
            assert result.output.contains(it.value)
        }
    }

    def "verify com.linkedin.python-sdist task list"() {
        given:
        buildFile << """
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        """.stripMargin().stripIndent()

        when:
        def result = run(TASKS, ["--all"])

        then: "make sure it passes initially first"
        sdistTasks.each { StandardTextValuesTasks it ->
            assert result.output.contains(it.value)
        }
    }
}
