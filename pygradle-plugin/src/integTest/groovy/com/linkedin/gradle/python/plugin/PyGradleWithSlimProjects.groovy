package com.linkedin.gradle.python.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification


/**
 * This test class is designed to test scenarios where we are only using pygradle for documentation and
 * other scenarios where it may be included in the project, but not active.
 */
class PyGradleWithSlimProjects extends Specification {

    @Rule
    final DefaultBlankProjectLayoutRule testProjectDir = new DefaultBlankProjectLayoutRule()

    def setup(){

    }

    def "verify works with blank project"() {
        given:
        testProjectDir.buildFile << """
        plugins {
            id 'com.linkedin.python'
        }
        
        ${PyGradleTestBuilder.createRepoClosure()}
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('build', 'coverage', '-s')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:build").outcome == TaskOutcome.SUCCESS

        // "Build will skip things that it should"
        result.output.contains("Flake8 config file doesn't exist, creating default")
        result.output.contains("BUILD SUCCESS")
        result.output.contains("[SKIPPING]")

        // "coverage should be skipped because lack of tests should disable it"
        result.output.contains(":coverage SKIPPED")

        // "install should be aborted because there is no setup.py"
        result.output.contains("[ABORTED]")
        result.output.contains("setup.py missing, skipping venv install for product")

        when:
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('buildDocsHtml')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SUCCESS
        result.output.contains("Sphinx docs dir doesn't exist.  Aborting")
    }

    def "verify docs only nothing else"() {
        given:
        testProjectDir.buildFile << """
        plugins {
            id 'com.linkedin.python'
        }
        
        ${PyGradleTestBuilder.createRepoClosure()}
        """

        testProjectDir.copyBuildDocsInfo()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('buildDocsHtml')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "make sure it passes initially first"
        result.task(":${testProjectDir.PROJECT_NAME_DIR}:buildDocsHtml").outcome == TaskOutcome.SUCCESS
        result.output.contains("Running Sphinx v1.4.1")
        result.output.contains("building [html]: targets for 1 source files that are out of date")
        result.output.contains("copying static files... done")
        result.output.contains("dumping object inventory... done")
    }
}
