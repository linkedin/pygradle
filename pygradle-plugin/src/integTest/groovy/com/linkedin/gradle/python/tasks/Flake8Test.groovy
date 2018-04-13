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

class Flake8Test extends Specification {
    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    def "a passing flake8"() {
        given:
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()

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
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()

        def baxPy = new File(testProjectDir.root, 'foo/src/foo/baz.py')
        baxPy.text = '''
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
}
