package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.LiPythonComponent
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TestLiPythonComponent extends Specification {

    def project = new ProjectBuilder().build()
    def settings = new LiPythonComponent(project)

    def "interpreterPath without interpreterVersion"() {
        expect: "default system Python without any settings"
        settings.interpreterPath == '/usr/bin/python2.6'
    }

    def "interpreterPath with interpreterVersion"() {
        when: "we request 2.7"
        settings.interpreterVersion = '2.7'
        then: "we get either cleanpython27 or system Python 2.7 if cleanpython is not installed"
        (settings.interpreterPath == '/export/apps/python/2.7/bin/python2.7' ||
                settings.interpreterPath == '/usr/bin/python2.7')
    }

    def "interpreterPath with unsupported interpreterVersion"() {
        when: "we request an unsupported version"
        settings.interpreterVersion = '2.5'
        then: "we get the default system Python"
        settings.interpreterPath == '/usr/bin/python2.6'
    }

    def "interpreterPath with unsupported major only interpreterVersion"() {
        when: "we request an unsupported major only version"
        settings.interpreterVersion = '1'
        then: "we get the default system Python"
        settings.interpreterPath == '/usr/bin/python2.6'
    }

    def "interpreterPath with major only 2 interpreterVersion"() {
        when: "we request only the major version 2"
        settings.interpreterVersion = '2'
        then: "we get the default major version 2 cleanpython or the system default if cleanptyhon is not installed"
        (settings.interpreterPath == '/export/apps/python/2.6/bin/python2.6' ||
                settings.interpreterPath == '/usr/bin/python2.6')
    }

    def "interpreterPath with major only 3 interpreterVersion"() {
        when: "we request only the major version 3"
        settings.interpreterVersion = '3'
        then: "we get the default major version 3 cleanpython or the system default if cleanpython is not installed"
        (settings.interpreterPath == '/export/apps/python/3.5/bin/python3.5' ||
                settings.interpreterPath == '/usr/bin/python2.6')
    }

    def "interpreterPath with systemPython set"() {
        when: "we have old systemPython setting"
        settings.interpreterVersion = '2.6'
        settings.systemPython = '/export/apps/python/2.7/bin/python2.7'
        then: "we get that as interpreter path"
        settings.interpreterPath == '/export/apps/python/2.7/bin/python2.7'
    }

    def "interpreterPath with nonsense interpreterVersion"() {
        when: "we request a nonsense version and try to get the interpreter path"
        settings.interpreterVersion = 'x.y'
        settings.interpreterPath
        then: "the exception is thrown"
        thrown(NumberFormatException)
    }

    def "interpreterPath is not settable"() {
        when: "we try to set interpreterPath"
        settings.interpreterPath = '/export/apps/python3.5/bin/python3.5'
        then: "we get an exception"
        thrown(GradleException)
    }

    def "pythonVersion and pythonMajorMinor are good"() {
        when: "we set interpreterVersion"
        settings.interpreterVersion = '2.7'
        then: "we get correct pythonVersion and pythonMajorMinor"
        settings.pythonVersion.startsWith('2.7.')
        settings.pythonMajorMinor == '2.7'
    }

    def "pythonEnvironment path"() {
        when: "path parts are separated"
        List<String> parts = settings.pythonEnvironment.get('PATH').toString().tokenize(':')
        then: "they have venv python PATH + system env PATH"
        !parts.empty
        parts.size() > 1
        parts[0].endsWith('/build/venv/bin')
        parts.contains('/bin')
        parts.contains('/usr/bin')
    }

}
