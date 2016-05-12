package com.linkedin.gradle.python.plugin


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonCliDistributionPluginTest extends Specification {

    def 'can apply python cli plugin resource'() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('python-cli')
    }

}
