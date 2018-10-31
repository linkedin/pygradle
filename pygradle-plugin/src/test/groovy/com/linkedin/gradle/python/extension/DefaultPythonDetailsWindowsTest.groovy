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
import com.linkedin.gradle.python.util.WindowsBinaryUnpacker
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Specification

import java.nio.file.Paths

import static com.linkedin.gradle.python.util.WindowsBinaryUnpacker.buildPythonExec

@Requires({ OperatingSystem.current() == OperatingSystem.WINDOWS })
class DefaultPythonDetailsWindowsTest extends Specification {

    class CustomTemporaryFolder extends TemporaryFolder {
        protected void after() {

        }
    }

    @Rule
    CustomTemporaryFolder temporaryFolder = new CustomTemporaryFolder()
    def project = new ProjectBuilder().build()
    DefaultPythonDetails settings

    def setup() {
        settings = new DefaultPythonDetails(project)
        addExecutables(settings)
        buildPythonExec(temporaryFolder.newFolder('python3.5.1', PythonDetailsFactory.getPythonApplicationDirectory()), WindowsBinaryUnpacker.PythonVersion.PYTHON_35)
    }

    void addExecutables(DefaultPythonDetails details) {
        WindowsBinaryUnpacker.PythonVersion.values().each {
            def folder = temporaryFolder.newFolder("python${ it.major }.${ it.minor }", PythonDetailsFactory.getPythonApplicationDirectory())
            details.prependExecutableDirectory(buildPythonExec(folder, it))
        }
    }

    def "interpreterPath without interpreterVersion"() {
        expect: "default system Python without any details"
        settings.getSystemPythonInterpreter()
    }

    def "interpreterPath with interpreterVersion"() {
        when: "we request 2.7"
        settings.pythonVersion = '2.7'
        then: "we get 2.7"
        settings.getSystemPythonInterpreter().path.endsWith("2.7.exe")
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
        settings.pythonVersion = '2'
        then: "we get the default major version 2 cleanpython or the system default if cleanpython is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("2.7.exe")
    }

    def "interpreterPath with major only 3 interpreterVersion"() {
        when: "we request only the major version 3"
        settings.pythonVersion = '3'
        then: "we get the default major version 3 cleanpython or the system default if cleanpython is not installed"
        settings.getSystemPythonInterpreter().path.endsWith("3.5.exe")
    }

    def "interpreterPath with systemPython set"() {
        when: "we have old systemPython setting"
        settings.pythonVersion = '2.7'
        def path = Paths.get(temporaryFolder.getRoot().absolutePath, 'python3.5.1',
                             DefaultVirtualEnvironment.getPythonApplicationDirectory(), 'python3.5.exe').toString()
        settings.systemPythonInterpreter = path
        then: "we get that as interpreter path"
        settings.systemPythonInterpreter.absolutePath == path
    }

    def "interpreterPath with nonsense interpreterVersion"() {
        when: "we request a nonsense version and try to get the interpreter path"
        settings.pythonVersion = 'x.y'
        then: "the exception is thrown"
        thrown(RuntimeException)
    }
}
