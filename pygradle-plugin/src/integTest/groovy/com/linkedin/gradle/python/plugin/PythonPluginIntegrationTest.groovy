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

import com.linkedin.gradle.python.plugin.testutils.DefaultProjectLayoutRule
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
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
        |    id 'com.linkedin.python-wheel-dist'
        |}
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', 'coverage', '-s', '-i')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:

        result.output.contains("BUILD SUCCESS")
        result.output.contains("test${File.separatorChar}test_a.py ..")
        result.output.contains('--- coverage: ')
        result.output.contains("src${File.separatorChar}foo${File.separatorChar}hello")
        result.output.contains('TOTAL')
        result.output.contains('Coverage HTML written to dir htmlcov')
        result.output.contains('Coverage XML written to file coverage.xml')
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':foo:getProbedTags').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        result.task(':foo:pytest').outcome == TaskOutcome.SUCCESS
        result.task(':foo:check').outcome == TaskOutcome.SUCCESS
        result.task(':foo:build').outcome == TaskOutcome.SUCCESS
        // The coverage task should run since 'coverage' was passed explicitly as an argument
        result.task(':foo:coverage').outcome == TaskOutcome.SUCCESS

        when:
        println "========================"
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', 'coverage', '-i')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: //Build will skip things that it should
        result.output.contains("BUILD SUCCESS")
        result.task(':foo:coverage').outcome == TaskOutcome.UP_TO_DATE
        !installOrderSorted(result.output, ':foo:installSetupRequirements', ':foo:installBuildRequirements')
        !installOrderSorted(result.output, ':foo:installBuildRequirements', ':foo:installPythonRequirements')
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
        |   coverage {
        |       run = true
        |   }
        |}
        |
        |buildDir = 'build2'
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', '-i')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:
        !new File(testProjectDir.getRoot(), 'foo/build').exists()
        new File(testProjectDir.getRoot(), 'foo/build2').exists()
        result.output.contains("BUILD SUCCESS")
        result.output.contains("test${File.separatorChar}test_a.py ..")
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':foo:getProbedTags').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        // The coverage task should run since we set it in the build.gradle file
        result.task(':foo:coverage').outcome == TaskOutcome.SUCCESS
        // The pytest task should be skipped since we are running coverage
        result.task(':foo:pytest').outcome == TaskOutcome.SKIPPED
        result.task(':foo:check').outcome == TaskOutcome.SUCCESS
        result.task(':foo:build').outcome == TaskOutcome.SUCCESS
        !installOrderSorted(result.output, ':foo:installSetupRequirements', ':foo:installBuildRequirements')
        !installOrderSorted(result.output, ':foo:installBuildRequirements', ':foo:installPythonRequirements')
    }

    def installOrderSorted(String output, String start, String end) {
        def beginning = output.indexOf(start)
        def ending = output.lastIndexOf(end)
        def lines = output.substring(beginning, ending).readLines()
        def collected = []
        def sorted = []
        for (String line : lines) {
            if (line.contains("Installing ")) {
                collected.add(line)
                sorted.add(line)
            }
        }
        sorted.sort()
        return collected == sorted
    }

    def "test pytest and coverage failure"() {
        when:
        testProjectDir.buildFile << """
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |repositories {
        |   pyGradlePyPi()
        |}
        |python {
        |   coverage {
        |       run = true
        |   }
        |}
        """.stripMargin().stripIndent()

        testProjectDir.setupCfg.text = """
        | [flake8]
        | ignore = E121,E123,E226,W292
        | max-line-length = 160
        |
        | [tool:pytest]
        | addopts = --ignore build/ --ignore dist/
        | testpaths = test/
        |
        | [coverage:report]
        | fail_under = ${failUnder}
        | show_missing = true
        |
        | [coverage:run]
        | branch = true
        | omit =
        |
        """.stripMargin().stripIndent()

        testProjectDir.testFile << """
        |
        | def test_insanity():
        |     assert ${assertionVal}
        |
        """.stripMargin().stripIndent()

        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', '-i')
            .withPluginClasspath()
            .withDebug(true)
            .buildAndFail()
        println result.output

        then:
        println result.output
        result.task(':foo:coverage').outcome == TaskOutcome.FAILED

        where:
        failUnder | assertionVal
        "100"     | "True"
        "100"     | "False"
        "10"      | "False"
    }
}
