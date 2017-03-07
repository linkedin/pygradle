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
