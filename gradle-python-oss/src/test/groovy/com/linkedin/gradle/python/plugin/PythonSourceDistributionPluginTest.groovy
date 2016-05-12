package com.linkedin.gradle.python.plugin


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonSourceDistributionPluginTest extends Specification {

    def "can apply python plugin class"() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('python-sdist')
    }

}
