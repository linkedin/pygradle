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

import com.linkedin.gradle.python.extension.internal.DefaultPythonDetails
import com.linkedin.gradle.python.util.OperatingSystem
import org.gradle.internal.impldep.org.apache.commons.lang.SerializationUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Specification


@Requires({ OperatingSystem.current() == OperatingSystem.UNIX })
class DefaultPythonDetailsUnixTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    def project = new ProjectBuilder().build()
    def details = new DefaultPythonDetails(project, new File("/foo/bar/venv"))

    def 'test serialization'() {
        expect:
        SerializationUtils.serialize(details)
    }

    def "interpreterPath without interpreterVersion"() {
        expect: "default system Python without any details"
        details.getSystemPythonInterpreter()
    }

    @Requires({ new File('/usr/bin/python2.7').exists() })
    def "interpreterPath with interpreterVersion"() {
        when: "we request 2.7"
        details.pythonVersion = '2.7'
        then: "we get 2.7"
        details.getSystemPythonInterpreter().path.endsWith("2.7")
    }

    @Requires({ !(new File('/usr/bin/python2.5').exists()) })
    def "interpreterPath with unsupported interpreterVersion"() {
        when: "we request an unsupported version"
        details.pythonVersion = '2.5'
        then: "we throw an exception"
        thrown(RuntimeException)
    }

    def "interpreterPath with unsupported major only interpreterVersion"() {
        when: "we request an unsupported major only version"
        details.pythonVersion = '1'
        then: "we throw an exception"
        thrown(RuntimeException)
    }

    @Requires({ new File('/usr/bin/python2.7').exists() })
    def "interpreterPath with major only 2 interpreterVersion"() {
        when: "we request only the major version 2"
        details.pythonVersion = '2'
        then: "we get the default major version 2 cleanpython or the system default if cleanptyhon is not installed"
        details.getSystemPythonInterpreter().path.endsWith("2.7")
    }

    @Requires({ new File('/usr/bin/python3.5').exists() })
    def "interpreterPath with major only 3 interpreterVersion"() {
        when: "we request only the major version 3"
        details.pythonVersion = '3'
        then: "we get the default major version 3 cleanpython or the system default if cleanpython is not installed"
        details.getSystemPythonInterpreter().path.endsWith("3.5")
    }

    @Requires({ new File('/usr/bin/python2.7').exists() && new File('/usr/bin/python3.6').exists() })
    def "interpreterPath with systemPython set"() {
        when: "we have old systemPython setting"
        details.pythonVersion = '2.7'
        details.systemPythonInterpreter = '/usr/bin/python3.6'
        then: "we get that as interpreter path"
        details.systemPythonInterpreter.absolutePath == '/usr/bin/python3.6'
    }

    def "interpreterPath with nonsense interpreterVersion"() {
        when: "we request a nonsense version and try to get the interpreter path"
        details.pythonVersion = 'x.y'
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
        details.prependExecutableDirectory(shadowFakePython.parentFile)
        details.prependExecutableDirectory(fakePython.parentFile)
        details.pythonVersion = '3.5'

        then:
        details.systemPythonInterpreter == fakePython
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
        details.appendExecutableDirectory(fakePython.parentFile)
        details.appendExecutableDirectory(shadowFakePython.parentFile)
        details.pythonVersion = '2.1'

        then:
        details.systemPythonInterpreter == fakePython
    }
}
