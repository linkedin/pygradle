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
import org.gradle.testkit.runner.TaskOutcome

import static com.linkedin.gradle.python.util.values.PyGradleTask.BUILD_DOCS_HTML

class DocsIntegrationTest extends AbstractPluginIntegrationSpec {



    def "verify docs only nothing else"() {
        given:
        temporaryFolder = new DefaultBlankProjectLayoutRule()
        temporaryFolder.before()

        temporaryFolder.buildFile << """
        | plugins {
        |     id 'com.linkedin.python-sphinx'
        | }
        | ${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        temporaryFolder.copyBuildDocsInfo()

        when:
        def result = run(BUILD_DOCS_HTML)

        then: "make sure it passes initially first"
        result.task(":${temporaryFolder.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SUCCESS
        result.output.contains("Running Sphinx v1.4.1")
        result.output.contains("building [html]: targets for 1 source files that are out of date")
        result.output.contains("copying static files")
        result.output.contains("dumping object inventory")
    }
}
