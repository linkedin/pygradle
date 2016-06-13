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
