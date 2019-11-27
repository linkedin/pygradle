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
package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.plugin.testutils.DefaultProjectLayoutRule
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

class Flake8TaskIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()
    def bazPy

    def setup() {
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()
        bazPy = new File(testProjectDir.root, 'foo/src/foo/baz.py')
    }

    def "a passing flake8"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('flake8', '-s', '-i')
            .withPluginClasspath()
            .build()
        println result.output

        then:
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
    }

    def "a failing flake8"() {
        given:
        bazPy.text = '''
        |import os, sys
        '''.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('flake8', '-s', '-i')
            .withPluginClasspath()
            .buildAndFail()
        println result.output

        then:
        result.output.contains('baz.py:2:10: E401 multiple imports on one line')
        result.task(':foo:flake8').outcome == TaskOutcome.FAILED
    }

    def "flake8 fails even with ignore"() {
        given:
        testProjectDir.buildFile << '''
        | import com.linkedin.gradle.python.tasks.Flake8Task
        | tasks.withType(Flake8Task) { Flake8Task task ->
        |     task.setIgnoreRules(["E401"] as Set)
        | }
        '''.stripMargin().stripIndent()

        bazPy.text = '''
        |import os, sys
        |'''.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('flake8', '-s', '-i')
            .withPluginClasspath()
            .buildAndFail()
        println result.output

        then:
        result.output.contains("baz.py:2:1: F401 'os' imported but unused")
        result.task(':foo:flake8').outcome == TaskOutcome.FAILED
    }

    def "warning for a newly failing flake8"() {
        given:
        testProjectDir.buildFile << '''
        | import com.linkedin.gradle.python.tasks.Flake8Task
        | tasks.withType(Flake8Task) { Flake8Task task ->
        |     task.setIgnoreRules(["E401", "F401"] as Set)
        | }
        '''.stripMargin().stripIndent()

        bazPy.text = '''
        |import os, sys
        |'''.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('flake8', '-s', '-i')
            .withPluginClasspath()
            .build()
        println result.output

        then:
        result.output.contains('The flake8 version has been recently updated, which added the following new rules:')
        result.output.contains('baz.py:2:10: E401 multiple imports on one line')
        result.output.contains("baz.py:2:1: F401 'os' imported but unused")
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
    }
}
