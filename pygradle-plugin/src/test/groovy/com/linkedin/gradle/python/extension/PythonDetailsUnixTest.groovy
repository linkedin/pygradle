/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.extension

import com.linkedin.gradle.python.util.OperatingSystem
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Specification


@Requires({ OperatingSystem.current() == OperatingSystem.UNIX })
class PythonDetailsUnixTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    def project = new ProjectBuilder().build()
    def settings = new PythonDetails(project)

    def "interpreterPath without interpreterVersion"() {
        expect: "default system Python without any settings"
        settings.getSystemPythonInterpreter()
    }

    @Requires({ new File('/usr/bin/python2.7').exists() })
    def "interpreterPath with interpreterVersion"() {
        when: "we request 2.7"
        settings.pythonVersion = '2.7'
        then: "we get 2.7"
        settings.getSystemPythonInterpreter().path.endsWith("2.7")
    }

    @Requires({ !(new File('/usr/bin/python2.5').exists()) })
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

    @Requires({ new File('/usr/bin/python2.6').exists() })
    def "interpreterPath with major only 2 interpreterVersion"() {
        when: "we request only the major version 2"
        settings.pythonVersion = '2'
        then: "we get the default major version 2 cleanpython or the system default if cleanptyhon is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("2.6")
    }

    @Requires({ new File('/usr/bin/python3.5').exists() })
    def "interpreterPath with major only 3 interpreterVersion"() {
        when: "we request only the major version 3"
        settings.pythonVersion = '3'
        then: "we get the default major version 3 cleanpython or the system default if cleanpython is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("3.5")
    }

    @Requires({ new File('/usr/bin/python2.7').exists() && new File('/usr/bin/python2.6').exists() })
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

    def "can prepend search path"() {
        temporaryFolder.newFolder('foo')
        temporaryFolder.newFolder('bar')
        def fakePython = temporaryFolder.newFile('foo/python3.5')
        fakePython.text = "#!/bin/bash\necho Python 3.5.6"
        fakePython.executable = true

        def shadowFakePython = temporaryFolder.newFile('bar/python3.5')
        shadowFakePython.text = "#!/bin/bash\necho Python 3.5.6"
        shadowFakePython.executable = true

        when:
        settings.prependExecutableDirectory(shadowFakePython.parentFile)
        settings.prependExecutableDirectory(fakePython.parentFile)
        settings.pythonVersion = '3.5'

        then:
        settings.systemPythonInterpreter == fakePython
    }

    def "can append search path"() {
        temporaryFolder.newFolder('foo')
        temporaryFolder.newFolder('bar')
        def fakePython = temporaryFolder.newFile('foo/python2.1')
        fakePython.text = "#!/bin/bash\necho Python 2.1"
        fakePython.executable = true

        def shadowFakePython = temporaryFolder.newFile('bar/python2.1')
        shadowFakePython.text = "#!/bin/bash\necho Python 2.1"
        shadowFakePython.executable = true

        when:
        settings.appendExecutableDirectory(fakePython.parentFile)
        settings.appendExecutableDirectory(shadowFakePython.parentFile)
        settings.pythonVersion = '2.1'

        then:
        settings.systemPythonInterpreter == fakePython
    }
}
