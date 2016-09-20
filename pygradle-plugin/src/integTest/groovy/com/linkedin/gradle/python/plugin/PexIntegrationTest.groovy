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

class PexIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    def "can build thin pex"() {
        given:
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python-pex'
        |}
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', '--stacktrace')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        println "ls ${testProjectDir.getRoot().getAbsolutePath()}/build/deployable/bin/".execute().text

        then:

        result.output.contains("BUILD SUCCESS")
        result.task(':flake8').outcome == TaskOutcome.SUCCESS
        result.task(':installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':installProject').outcome == TaskOutcome.SUCCESS
        result.task(':pytest').outcome == TaskOutcome.SUCCESS
        result.task(':check').outcome == TaskOutcome.SUCCESS
        result.task(':build').outcome == TaskOutcome.SUCCESS
        result.task(':buildPex').outcome == TaskOutcome.SUCCESS

        new File(testProjectDir.getRoot(), "build/deployable/bin/hello_world").exists()
        new File(testProjectDir.getRoot(), "build/deployable/bin/testProject.pex").exists()

        when: "we have a pex file"
        def line
        new File(testProjectDir.getRoot(), "build/deployable/bin/testProject.pex").withReader { line = it.readLine() }

        then: "its shebang line is not pointing to a virtualenv"
        line.startsWith("#!") && !line.contains("venv")

        when:
        def out = new StringBuilder()
        def proc = "${testProjectDir.getRoot().getAbsolutePath()}/build/deployable/bin/hello_world".execute()
        proc.consumeProcessOutput(out, out)
        proc.waitForOrKill(1000)
        println out.toString()

        then:
        out.toString() == "Hello World\n"
    }

    def "can build fat pex"() {
        given:
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python-pex'
        |}
        |
        |python {
        |  pex {
        |    fatPex = true
        |  }
        |}
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', '--stacktrace')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        println "ls ${testProjectDir.getRoot().getAbsolutePath()}/build/deployable/bin/".execute().text

        then:

        result.output.contains("BUILD SUCCESS")
        result.task(':flake8').outcome == TaskOutcome.SUCCESS
        result.task(':installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':installProject').outcome == TaskOutcome.SUCCESS
        result.task(':pytest').outcome == TaskOutcome.SUCCESS
        result.task(':check').outcome == TaskOutcome.SUCCESS
        result.task(':build').outcome == TaskOutcome.SUCCESS
        result.task(':buildPex').outcome == TaskOutcome.SUCCESS

        new File(testProjectDir.getRoot(), "build/deployable/bin/hello_world").exists()

        when: "we have a pex file"
        def line
        new File(testProjectDir.getRoot(), "build/deployable/bin/hello_world").withReader { line = it.readLine() }

        then: "its shebang line is not pointing to a virtualenv"
        line.startsWith("#!") && !line.contains("venv")

        when:
        def out = new StringBuilder()
        def proc = "${testProjectDir.getRoot().getAbsolutePath()}/build/deployable/bin/hello_world".execute()
        proc.consumeProcessOutput(out, out)
        proc.waitForOrKill(1000)
        println out.toString()

        then:
        out.toString() == "Hello World\n"
    }
}
