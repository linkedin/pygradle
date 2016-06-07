package com.linkedin.gradle.python.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


class PythonPluginIntegrationTest extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "hello world task prints hello world"() {
        given:
        buildFile << '''
plugins {
    id 'python'
}

repositories {
    ivy {
        url 'http://artifactory.corp.linkedin.com:8081/artifactory/release/'
        layout "pattern", {
            ivy "[organisation]/[module]/[revision]/[module]-[revision].ivy"
            artifact "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
            m2compatible = true
        }
    }

    ivy {
        name 'pypi-external'
        url "http://artifactory.corp.linkedin.com:8081/artifactory/pypi-external"
        layout "pattern", {
            ivy "[module]/[revision]/[module]-[revision].ivy"
            artifact "[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
            m2compatible = true
        }
    }
}
        '''

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
        testProjectDir.newFile('setup.py') << GradleSetupPyBuilder.createSetupPy()

        // Create the setup.cfg file
        testProjectDir.newFile('setup.cfg') << GradleSetupCfgBuilder.createSetupCfg()

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
    }
}
