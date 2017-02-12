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

import java.nio.file.Paths

class PipIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()
    def "will write out pinned.txt"() {
        given:
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python'
        |}
        |
        |dependencies {
        |  python 'pypi:flake8:+'
        |}
        |
        |${PyGradleTestBuilder.createRepoClosure()}
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
        def file = testProjectDir.getRoot().toPath().resolve(Paths.get("foo", "pinned.txt")).toFile()
        file.exists()
        file.text ==~ "flake8==(.*?)${System.getProperty("line.separator")}"

        result.output.contains("BUILD SUCCESS")
        result.task(':foo:pinRequirements').outcome == TaskOutcome.SUCCESS

        when:
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:
        file.exists()
        file.text ==~ "flake8==(.*?)${System.getProperty("line.separator")}"

        result.output.contains("BUILD SUCCESS")
        result.task(':foo:pinRequirements').outcome == TaskOutcome.UP_TO_DATE

        when:
        testProjectDir.buildFile << """\
        |dependencies {
        |  python 'pypi:wheel:+'
        |}
        """.stripMargin().stripIndent()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:
        file.exists()
        file.readLines().collect { it.split("==")[0] } as Set == ["flake8", "wheel"] as Set

        result.output.contains("BUILD SUCCESS")
        result.task(':foo:pinRequirements').outcome == TaskOutcome.SUCCESS
    }

}
