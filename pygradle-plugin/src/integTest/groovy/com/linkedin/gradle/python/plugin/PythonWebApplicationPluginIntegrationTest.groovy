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
import com.linkedin.gradle.python.util.PexFileUtil
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class PythonWebApplicationPluginIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    def "can build web-app"() {
        given:
        testProjectDir.buildFile << """\
        |plugins {
        |    id 'com.linkedin.python-web-app'
        |}
        |
        |python {
        |  pex {
        |    fatPex = true
        |  }
        |}
        |
        |version = '1.2.3'
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

        result.output.contains("BUILD SUCCESS")
        result.output.contains("test${File.separatorChar}test_a.py ..")
        result.task(':foo:flake8').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':foo:createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        result.task(':foo:pytest').outcome == TaskOutcome.SUCCESS
        result.task(':foo:check').outcome == TaskOutcome.SUCCESS
        result.task(':foo:build').outcome == TaskOutcome.SUCCESS
        result.task(':foo:packageDeployable').outcome == TaskOutcome.SUCCESS
        result.task(':foo:packageWebApplication').outcome == TaskOutcome.SKIPPED
        Path deployablePath = testProjectDir.getRoot().toPath().resolve(Paths.get('foo', 'build', 'deployable', 'bin'))

        when: "we have a pex file"
        def line = new String(deployablePath.resolve(PexFileUtil.createFatPexFilename('hello_world')).bytes, "UTF-8").substring(0, 100)

        then: "its shebang line is not pointing to a virtualenv"
        line.startsWith("#!") && !line.contains("venv")

        when:
        def out = ExecUtils.run(deployablePath.resolve(PexFileUtil.createFatPexFilename('hello_world')))
        println out

        then:
        out.toString() == "Hello World${System.getProperty("line.separator")}".toString()

    }
}
