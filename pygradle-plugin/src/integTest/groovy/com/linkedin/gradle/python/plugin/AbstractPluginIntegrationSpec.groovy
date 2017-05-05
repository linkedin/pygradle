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
import com.linkedin.gradle.python.util.values.PyGradleTask
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Specification

class AbstractPluginIntegrationSpec extends Specification {

    enum RunType { PASS, FAIL }

    List<File> pluginClasspath = []

    @Rule
    DefaultProjectLayoutRule temporaryFolder = new DefaultProjectLayoutRule()

    File buildFile

    static pygradleSupportedGradleVersions = ['3.2']

    def setup() {
        buildFile = temporaryFolder.newFile('build.gradle')

        temporaryFolder.newFolder("src", "main", "java")
        temporaryFolder.newFolder("src", "main", "groovy")
    }


    File getIntegResourcesFile(String sPath) {
        File file = new File("src/integTest/resources" + sPath)
        new File(file.getAbsolutePath())
    }

    BuildResult run(PyGradleTask task, List<String> cmdOption = []) {
        runJob(RunType.PASS, null, task, cmdOption)
    }

    BuildResult run(String gradleVersion, PyGradleTask task, List<String> cmdOption = []) {
        runJob(RunType.PASS, gradleVersion, task, cmdOption)
    }

    BuildResult fail(PyGradleTask task, List<String> cmdOption = []) {
        runJob(RunType.FAIL, null, task, cmdOption)
    }

    BuildResult fail(String gradleVersion, PyGradleTask task, List<String> cmdOption = []) {
        runJob(RunType.FAIL, gradleVersion, task, cmdOption)
    }

    BuildResult runJob(RunType r, String gradleVersion, PyGradleTask task, List<String> cmdOption = []) {
        def writer = new StringWriter()
        GradleRunner runner = newRunner(task, writer, gradleVersion, cmdOption)

        def result

        switch (r) {
            case RunType.PASS:
                result = runner.build()
                break
            case RunType.FAIL:
                result = runner.buildAndFail()
                break
        }

        println writer
        return result
    }

    private GradleRunner newRunner(PyGradleTask task, StringWriter writer, String gradleVersion,
                                   List<String> cmdOption = []) {
        def gradleUserHome = temporaryFolder.newFolder('some-custom-user-home')

         cmdOption.add(0, task.value)
         cmdOption.addAll(["-g", gradleUserHome.absolutePath])

        def runner = GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withArguments( cmdOption)
                //.withPluginClasspath(pluginClasspath)
                .forwardStdOutput(writer)
                .withPluginClasspath()

        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }

        runner
    }
}
