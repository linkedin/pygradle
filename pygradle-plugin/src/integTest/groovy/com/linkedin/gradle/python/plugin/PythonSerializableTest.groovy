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

class PythonSerializableTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    def "can serialize inputs and outputs"() {
        given:
        testProjectDir.buildFile << """\
        |import com.linkedin.gradle.python.tasks.InstallVirtualEnvironmentTask
        |import com.linkedin.gradle.python.extension.PythonDetailsFactory
        |import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        |import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYTHON
        |
        |plugins {
        |    id 'com.linkedin.python-sdist'
        |}
        |version = '1.2.3'
        |${ PyGradleTestBuilder.createRepoClosure() }
        |apply plugin: com.linkedin.gradle.python.plugin.WheelFirstPlugin
        |
        |dependencies {
        |    python 'pypi:requests:2.20.0'
        |}
        |
        |
        |ext.anotherVenv = new File("\$buildDir/AnotherVenv")
        |
        |task installAnotherVenv ( type: InstallVirtualEnvironmentTask ) {
        |    pythonDetails = PythonDetailsFactory.makePythonDetails(project, anotherVenv)
        |}
        |
        |task installPythonRequirementsInAnotherVenv ( type: PipInstallTask ){
        |    dependsOn installAnotherVenv
        |    mustRunAfter installAnotherVenv
        |    pythonDetails = PythonDetailsFactory.makePythonDetails(project, anotherVenv)
        |    installFileCollection = project.getConfigurations().getByName(CONFIGURATION_PYTHON.getValue())
        |    outputs.dir anotherVenv
        |}
        """.stripMargin().stripIndent()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('installPythonRequirementsInAnotherVenv')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then:

        result.output.contains("BUILD SUCCESS")
        result.task(':foo:installPythonRequirementsInAnotherVenv').outcome == TaskOutcome.SUCCESS
    }
}
