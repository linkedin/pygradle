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

import com.linkedin.gradle.python.plugin.testutils.DefaultBlankProjectLayoutRule
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
import com.linkedin.gradle.python.util.StandardTextValues
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import spock.lang.Specification

/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 */
class CleanLeaveVenvTest extends Specification {

    @Rule
    final DefaultBlankProjectLayoutRule testProjectDir = new DefaultBlankProjectLayoutRule()

    def "Cleans everything from build except venv folder"() {

        given:
        testProjectDir.buildFile << """
        | plugins {
        |     id 'com.linkedin.python'
        | }
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        def fvenv = testProjectDir.newFolder(testProjectDir.PROJECT_NAME_DIR, "build", "venv")
        def ftest2 = testProjectDir.newFolder(testProjectDir.PROJECT_NAME_DIR, "build", "test2")
        def ftest1 = testProjectDir.newFile("${testProjectDir.PROJECT_NAME_DIR + "/build"}/test1.txt")

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(StandardTextValues.TASK_CLEAN_SAVE_VENV.value)
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        !ftest1.exists()
        !ftest2.exists()
        fvenv.exists()
    }
}
