package com.linkedin.gradle.python

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TestPythonExtension extends Specification {

    def project = new ProjectBuilder().build()

    def "pythonEnvironment path"() {
        when: "path parts are separated"
        def settings = new PythonExtension(project)
        List<String> parts = settings.pythonEnvironment.get('PATH').toString().tokenize(':')
        then: "they have venv python PATH + system env PATH"
        !parts.empty
        parts.size() > 1
        parts[0].endsWith('/build/venv/bin')
        parts.contains('/bin')
        parts.contains('/usr/bin')
    }

}
