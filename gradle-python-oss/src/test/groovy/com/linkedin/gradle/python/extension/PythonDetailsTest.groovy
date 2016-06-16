package com.linkedin.gradle.python.extension

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Requires
import spock.lang.Specification


class PythonDetailsTest extends Specification {

    def project = new ProjectBuilder().build()
    def settings = new PythonDetails(project, null)

    def "interpreterPath without interpreterVersion"() {
        expect: "default system Python without any settings"
        settings.getSystemPythonInterpreter()
    }

    def "interpreterPath with interpreterVersion"() {
        when: "we request 2.7"
        settings.pythonVersion = '2.7'
        then: "we get 2.7"
        settings.getSystemPythonInterpreter().path.endsWith("2.7")
    }

    def "interpreterPath with unsupported interpreterVersion"() {
        when: "we request an unsupported version"
        settings.pythonVersion = '2.5'
        then: "we throw an exception"
        thrown(RuntimeException)
    }

    def "interpreterPath with unsupported major only interpreterVersion"() {
        when: "we request an unsupported major only version"
        settings.pythonVersion = '1'
        then: "we throw an exception"
        thrown(RuntimeException)
    }

    def "interpreterPath with major only 2 interpreterVersion"() {
        when: "we request only the major version 2"
        settings.pythonVersion= '2'
        then: "we get the default major version 2 cleanpython or the system default if cleanptyhon is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("2.6")
    }

    def "interpreterPath with major only 3 interpreterVersion"() {
        when: "we request only the major version 3"
        settings.pythonVersion = '3'
        then: "we get the default major version 3 cleanpython or the system default if cleanpython is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("3.5")
    }

    @Requires({ new File('/usr/bin/python2.7').exists() })
    def "interpreterPath with systemPython set"() {
        when: "we have old systemPython setting"
        settings.pythonVersion = '2.6'
        settings.systemPythonInterpreter = '/usr/bin/python2.7'
        then: "we get that as interpreter path"
        settings.systemPythonInterpreter.absolutePath == '/usr/bin/python2.7'
    }

    def "interpreterPath with nonsense interpreterVersion"() {
        when: "we request a nonsense version and try to get the interpreter path"
        settings.pythonVersion = 'x.y'
        then: "the exception is thrown"
        thrown(RuntimeException)
    }
}
