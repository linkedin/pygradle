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
import org.junit.Rule
import spock.lang.Specification

class FailureReasonProviderIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    @SuppressWarnings("GStringExpressionWithinString")
    def 'will report errors nicely'() {
        given:
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | configurations {
        |    testProj
        | }
        |
        | dependencies {
        |     testProj 'pypi:pyflakes:+'
        | }
        |
        | task testWheelTask(type: com.linkedin.gradle.python.tasks.BuildWheelsTask) {
        |   dependsOn 'installProject'
        |   packageSettings = new DefaultTestPackageSettings(project.projectDir)
        |   installFileCollection = configurations.testProj
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        | import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getSupportedLanguageVersions(PackageInfo packageInfo) {
        |         return ['2.8']
        |     }
        | }
        |
        | class MyTaskListener implements TaskExecutionListener {
        |
        |     @Override
        |     void beforeExecute(Task task) {
        |     }
        |
        |     @Override
        |     void afterExecute(Task task, TaskState taskState) {
        |         if (task instanceof FailureReasonProvider && taskState.failure != null) {
        |             println(task.reason.readLines().collect { "#\${task.name}>> \$it" }.join("\\n"))
        |         }
        |     }
        | }
        |
        | project.gradle.addListener(new MyTaskListener())
        |
        | version = '1.0.0'
        |
        | // allow it to build wheels by disabling layered wheel cache
        | import com.linkedin.gradle.python.tasks.supports.SupportsWheelCache
        | import com.linkedin.gradle.python.wheel.EmptyWheelCache
        | afterEvaluate {
        |     def wheelCache = new EmptyWheelCache()
        |     tasks.withType(SupportsWheelCache) { SupportsWheelCache task ->
        |         task.wheelCache = wheelCache
        |     }
        | }
        | ${ PyGradleTestBuilder.createRepoClosure() }
        """.stripMargin().stripIndent()

        testProjectDir.newFile('foo/test/foo.py').text = "import os #something"

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('testWheelTask', "pytest", "flake8", '--continue')
            .withPluginClasspath()
            .withDebug(true)
            .buildAndFail()
        println result.output

        def outputLines = result.output.readLines()
            .findAll { it.startsWith("#") && it.contains(">>") }
            .collect { it.replace(testProjectDir.root.absolutePath, '/root') }

        then:
        // Output structure is "#${taskName}>> ${message}
        // Map is task name to lines it should have. Each line is a "contains" operation, not equals.
        def expectedErrors = [
            'testWheelTask': [
                'Package pyflakes works only with Python versions: [2.8]'
            ],
            'pytest'       : [
                "Traceback:",
                "test/test_a.py:1: in <module>",
                "    from foo.hello import generate_welcome",
                "E   ImportError: No module named hello",
            ],
            'flake8'       : [
                "/foo/test/foo.py:1:1: F401 'os' imported but unused",
                "/foo/test/foo.py:1:10: E261 at least two spaces before inline comment",
                "/foo/test/foo.py:1:11: E262 inline comment should start with '# '"
            ]
        ]
        expectedErrors.each { task, lines ->
            println("Checking for errors reported by task $task")
            def taskLines = outputLines
                .findAll { it.startsWith("#$task>> ") }
                .collect { it.replace("#$task>> ", '') }
            lines.each { line ->
                assert taskLines.any { it.contains(line) }
            }
        }
    }
}
