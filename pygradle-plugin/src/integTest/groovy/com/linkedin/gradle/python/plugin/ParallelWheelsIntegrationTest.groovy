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
import com.linkedin.gradle.python.plugin.testutils.ExecUtils
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
import com.linkedin.gradle.python.util.OperatingSystem
import com.linkedin.gradle.python.util.PexFileUtil
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class ParallelWheelsIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    @IgnoreIf({ OperatingSystem.current() == OperatingSystem.WINDOWS })
    def "can build thin pex"() {

        given:
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        | apply plugin: com.linkedin.gradle.python.plugin.WheelFirstPlugin
        | version = '1.0.0'
        | python {
        |   pex {
        |     fatPex = false
        |   }
        | }
        | ${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', '--stacktrace', '--info')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        Path deployablePath = testProjectDir.root.toPath().resolve(Paths.get('foo', 'build', 'deployable', "bin"))

        then:

        result.output.contains("BUILD SUCCESS")
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
        result.task(':foo:parallelWheels').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        result.task(':foo:pytest').outcome == TaskOutcome.SUCCESS
        result.task(':foo:check').outcome == TaskOutcome.SUCCESS
        result.task(':foo:build').outcome == TaskOutcome.SUCCESS
        result.task(':foo:assembleContainers').outcome == TaskOutcome.SUCCESS

        deployablePath.resolve('hello_world').toFile().exists()
        deployablePath.resolve(PexFileUtil.createThinPexFilename('foo')).toFile().exists()

        when: "we have a pex file"
        def line = new String(deployablePath.resolve(PexFileUtil.createThinPexFilename('foo')).bytes, "UTF-8").substring(0, 100)

        then: "its shebang line is not pointing to a virtualenv"
        line.startsWith("#!") && !line.contains("venv")

        when:
        def out = ExecUtils.run(deployablePath.resolve('hello_world'))
        println out

        then:
        out.toString() == "Hello World${ System.getProperty("line.separator") }".toString()
    }

    @IgnoreIf({ OperatingSystem.current() == OperatingSystem.WINDOWS })
    def "installs setup requires for parallelWheels task"() {
        given:
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | apply plugin: com.linkedin.gradle.python.plugin.WheelFirstPlugin
        |
        | version = '1.0.0'
        | ${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('parallelWheels', '--stacktrace', '--info')
            .withPluginClasspath()
            .build()
        println result.output

        then:
        Path venvPath = testProjectDir.root.toPath().resolve(Paths.get('foo', 'build', 'venv'))

        def output = ExecUtils.run(venvPath.resolve("bin/pip"), "freeze", "--all")
        println(output)
        output.contains("wheel==")
        output.contains("pip==")
        output.contains("setuptools==")
        result.task(':foo:findPythonAbi') == null //task was removed and is no longer needed
        result.task(':foo:parallelWheels').outcome == TaskOutcome.SUCCESS

        when:
        println "======================="
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('pytest', '--stacktrace', '--info')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:
        result.task(':foo:pytest').outcome == TaskOutcome.SUCCESS  //using pytest since it will always require deps
        def taskOutcome = result.task(':foo:parallelWheels').outcome
        taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE
    }
}
