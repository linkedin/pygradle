package com.linkedin.gradle.python.plugin
import nebula.test.IntegrationSpec

class PythonLangPluginIntegrationTest extends IntegrationSpec {

    def 'test things'() {
        setup:
        buildFile << """
        buildscript {
            dependencies {
                classpath files(${System.getenv('test.dependencies').split(',').collect{ "'$it'" }.join(',')})
            }
        }
        """.stripIndent()
        buildFile << "apply plugin: 'python'"

        when:
        def executionResult = runTasks('model')

        then:
        executionResult.standardOutput == ''
    }
}
