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
package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.tasks.PipInstallTask
import com.linkedin.gradle.python.util.OperatingSystem
import com.linkedin.gradle.python.util.StandardTextValues
import com.linkedin.gradle.python.util.WindowsBinaryUnpacker
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.linkedin.gradle.python.util.WindowsBinaryUnpacker.buildPythonExec

class PythonPluginTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "can apply python plugin class"() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply(PythonPlugin)
    }

    def 'can apply python plugin resource'() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('com.linkedin.python')
    }

    def 'can apply python plugin twice'() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('com.linkedin.python')
        project.plugins.apply('com.linkedin.python')
    }

    def 'applied plugin has configurations'() {
        when:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.linkedin.python')
        then:
        for (c in [StandardTextValues.CONFIGURATION_DEFAULT.value,
                   StandardTextValues.CONFIGURATION_PYTHON.value,
                   StandardTextValues.CONFIGURATION_WHEEL.value,
                   StandardTextValues.CONFIGURATION_VENV.value]) {
            assert project.configurations.getByName(c)
        }
        assert (project.configurations.getByName(StandardTextValues.CONFIGURATION_DEFAULT.value).getExtendsFrom()*.name
            == [StandardTextValues.CONFIGURATION_PYTHON.value])
    }

    def 'can apply java'() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('java')
        project.plugins.apply(PythonPlugin)
    }

    def 'can find python version'() {
        when:
        def project = new ProjectBuilder().build()
        project.plugins.apply('com.linkedin.python')
        def details = project.getExtensions().getByType(PythonExtension).getDetails()

        if (OperatingSystem.current() == OperatingSystem.WINDOWS) {
            def folder = temporaryFolder.newFolder("python2.7", 'bin')
            details.prependExecutableDirectory(buildPythonExec(folder, WindowsBinaryUnpacker.PythonVersion.PYTHON_27))
        }

        then:
        details.pythonVersion.pythonVersion
    }

    def 'install project has the root file in the collection'() {
        setup:
        temporaryFolder.create()
        def projectDir = temporaryFolder.newFolder('projectDir')

        when:
        def project = new ProjectBuilder().withName('project').withProjectDir(projectDir).build()
        project.plugins.apply('com.linkedin.python')

        then:
        PipInstallTask install = (PipInstallTask) project.tasks.getByName(StandardTextValues.TASK_INSTALL_PROJECT.value)
        install.installFileCollection.getFiles().size() == 1
        install.installFileCollection.getFiles().first().getName() == projectDir.getName()
    }

    def 'product dependencies are installed after test dependencies'() {
        when:
        def project = new ProjectBuilder().build()
        project.plugins.apply(PythonPlugin)

        then:
        PipInstallTask installPythonReqs = (PipInstallTask) project.tasks.getByName(StandardTextValues.TASK_INSTALL_PYTHON_REQS.value)
        PipInstallTask installTestReqs = (PipInstallTask) project.tasks.getByName(StandardTextValues.TASK_INSTALL_TEST_REQS.value)
        installTestReqs.getDependsOn().contains(installPythonReqs) == false
        assert installPythonReqs.getDependsOn().contains(installTestReqs)
    }
}
