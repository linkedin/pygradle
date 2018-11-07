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

import com.linkedin.gradle.python.plugin.testutils.DefaultBlankProjectLayoutRule
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
import com.linkedin.gradle.python.util.OperatingSystem
import com.linkedin.gradle.python.util.StandardTextValues
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import java.nio.file.Paths

/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 */
class PyGradleWithSlimProjectsTest extends Specification {

    @Rule
    final DefaultBlankProjectLayoutRule testProjectDir = new DefaultBlankProjectLayoutRule()


    def "verify works with blank project with pipconfig"() {
        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | python{
        |     pipConfig = ['global':['index-url': 'https://<login>:<password>@your.repo.com/custom/url', 'timeout': '60']]
        |     for (String command : ['install', 'wheel', 'download']) {
        |         pipConfig.put(command, [:])
        |         pipConfig.get(command).put('no-build-isolation', 'false')
        |     }
        | }
        |
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        def fileExtension

        if (OperatingSystem.current().isWindows()) {
            fileExtension = "ini"
        } else {
            fileExtension = "conf"
        }

        File fpip = Paths.get(testProjectDir.root.absolutePath, testProjectDir.PROJECT_NAME_DIR, "build", "venv", "pip.${fileExtension}").toFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', 'coverage', '-s')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        // there was nothing to build, so it reports up to date
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:build").outcome == TaskOutcome.UP_TO_DATE
        result.output.contains("BUILD SUCCESS")

        // make sure the pip file is there with the correct contents
        fpip.exists()
        def lines = fpip.readLines()
        lines.get(0) == "[global]"
        lines.get(1) == "index-url = https://<login>:<password>@your.repo.com/custom/url"
        lines.get(2) == "timeout = 60"

        // "Build will skip things that it should"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:flake8").outcome == TaskOutcome.SKIPPED

        // "coverage should be skipped because lack of tests should disable it"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:coverage").outcome == TaskOutcome.SKIPPED

        // "install should be aborted because there is no setup.py"
        result.output.contains("[ABORTED]")
        result.output.contains("setup.py missing, skipping venv install for product")

        when: "you run the thing again, the venv should be reused and the docs should skip"
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('buildDocsHtml')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SKIPPED
        //result.output.contains("Sphinx docs dir doesn't exist.  Aborting")
    }

    def "verify docs only nothing else"() {
        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | python{
        |     pipConfig = ['global':['index-url': 'https://<login>:<password>@your.repo.com/custom/url', 'timeout': '60']]
        |     for (String command : ['install', 'wheel', 'download']) {
        |         pipConfig.put(command, [:])
        |         pipConfig.get(command).put('no-build-isolation', 'false')
        |     }
        | }
        |
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        testProjectDir.copyBuildDocsInfo()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('buildDocsHtml')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SUCCESS
        result.output.contains("Running Sphinx")
        result.output.contains("building [html]: targets for 1 source files that are out of date")
        result.output.contains("copying static files")
        result.output.contains("dumping object inventory")
    }

    def "verify pytest and coverage skip when nothing there"() {
        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(StandardTextValues.TASK_PYTEST.value, StandardTextValues.TASK_COVERAGE.value)
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:${StandardTextValues.TASK_PYTEST.value}").outcome == TaskOutcome.SKIPPED
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:${StandardTextValues.TASK_COVERAGE.value}").outcome == TaskOutcome.SKIPPED
    }
}
