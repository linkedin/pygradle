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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

class PythonPluginIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    def "can build library"() {
        given:
        testProjectDir.buildFile << """
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build', 'coverage')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        println result.output

        then:

        result.output.contains("BUILD SUCCESS")
        result.output.contains("test${File.separatorChar}test_a.py ..")
        result.output.contains('--- coverage: ')
        result.output.contains("src${File.separatorChar}foo${File.separatorChar}hello          5      1    80%")
        result.output.contains('TOTAL                  5      1    80%')
        result.output.contains('Coverage HTML written to dir htmlcov')
        result.output.contains('Coverage XML written to file coverage.xml')
        result.task(':flake8').outcome == TaskOutcome.SUCCESS
        result.task(':installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':installProject').outcome == TaskOutcome.SUCCESS
        result.task(':pytest').outcome == TaskOutcome.SUCCESS
        result.task(':check').outcome == TaskOutcome.SUCCESS
        result.task(':build').outcome == TaskOutcome.SUCCESS
        result.task(':coverage').outcome == TaskOutcome.SUCCESS
    }

    def "can use external library"() {
        given:
        testProjectDir.buildFile << """
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |repositories {
        |   pyGradlePyPi()
        |}
        |
        |python {
        |   details {
        |       virtualEnvPrompt = 'pyGradle!'
        |   }
        |}
        |
        |buildDir = 'build2'
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:

        !new File(testProjectDir.getRoot(), 'build').exists()
        new File(testProjectDir.getRoot(), 'build2').exists()
        result.output.contains("BUILD SUCCESS")
        result.output.contains("test${File.separatorChar}test_a.py ..")
        result.task(':flake8').outcome == TaskOutcome.SUCCESS
        result.task(':installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':installProject').outcome == TaskOutcome.SUCCESS
        result.task(':pytest').outcome == TaskOutcome.SUCCESS
        result.task(':check').outcome == TaskOutcome.SUCCESS
        result.task(':build').outcome == TaskOutcome.SUCCESS
    }
}
