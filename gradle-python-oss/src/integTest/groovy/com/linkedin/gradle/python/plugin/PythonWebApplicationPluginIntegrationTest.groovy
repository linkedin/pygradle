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
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class PythonWebApplicationPluginIntegrationTest extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "can build web-app"() {
        given:
        buildFile << """\
        |plugins {
        |    id 'python-web-app'
        |}
        |
        |version = '1.2.3'
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        testProjectDir.newFolder('test')
        testProjectDir.newFolder('src')

        // Create some code
        testProjectDir.newFile('src/hello.py') << '''\
            | def main():
            |     print 'Hello World'
            |
            |
            | if __name__ == '__main__':
            |     main()
            '''.stripMargin().stripIndent()

        // Create a setup file
        testProjectDir.newFile('setup.py') << PyGradleTestBuilder.createSetupPy()

        // Create the setup.cfg file
        testProjectDir.newFile('setup.cfg') << PyGradleTestBuilder.createSetupCfg()

        testProjectDir.newFile('settings.gradle') << PyGradleTestBuilder.createSettingGradle()

        // Create the test directory and a simple test
        testProjectDir.newFile('test/test_a.py') << '''\
            | def test_sanity():
            |     expected = 6
            |     assert 2 * 3 == expected
            '''.stripMargin().stripIndent()

        testProjectDir.newFile("MANIFEST.in") << ''

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
        result.output.contains('test/test_a.py .')
        result.task(':flake8').outcome == TaskOutcome.SUCCESS
        result.task(':installPythonRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':installTestRequirements').outcome == TaskOutcome.SUCCESS
        result.task(':createVirtualEnvironment').outcome == TaskOutcome.SUCCESS
        result.task(':installProject').outcome == TaskOutcome.SUCCESS
        result.task(':pytest').outcome == TaskOutcome.SUCCESS
        result.task(':check').outcome == TaskOutcome.SUCCESS
        result.task(':build').outcome == TaskOutcome.SUCCESS
        result.task(':packageWebApplication').outcome == TaskOutcome.SUCCESS
    }
}
