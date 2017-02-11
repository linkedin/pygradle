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
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 */
class PyGradleWithSlimProjectsTest extends Specification {

    @Rule
    final DefaultBlankProjectLayoutRule testProjectDir = new DefaultBlankProjectLayoutRule()

    def setup() {

    }

    def "verify works with blank project with pipconfig"() {
        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | python{
        |     pipConfig = ['global':['index-url': 'https://<login>:<password>@your.repo.com/custom/url', 'timeout': '60']]
        | }
        |
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', 'coverage', '-s')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:build").outcome == TaskOutcome.SUCCESS

        // "Build will skip things that it should"
        result.output.contains("Flake8 task skipped, no folders")
        result.output.contains("BUILD SUCCESS")
        result.output.contains("[SKIPPING]")

        // "coverage should be skipped because lack of tests should disable it"
        result.output.contains(":coverage SKIPPED")

        // "install should be aborted because there is no setup.py"
        result.output.contains("[ABORTED]")
        result.output.contains("setup.py missing, skipping venv install for product")

        when:
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('buildDocsHtml')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SUCCESS
        result.output.contains("Sphinx docs dir doesn't exist.  Aborting")
    }

    def "verify docs only nothing else"() {
        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | python{
        |     pipConfig = ['global':['index-url': 'https://<login>:<password>@your.repo.com/custom/url', 'timeout': '60']]
        | }
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
        result.output.contains("Running Sphinx v1.4.1")
        result.output.contains("building [html]: targets for 1 source files that are out of date")
        result.output.contains("copying static files")
        result.output.contains("dumping object inventory")
    }
}
