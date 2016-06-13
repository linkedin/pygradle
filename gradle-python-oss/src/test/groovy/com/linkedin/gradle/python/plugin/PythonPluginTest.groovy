package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.tasks.PipInstallTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

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
        project.plugins.apply('python')
    }

    def 'can apply python plugin twice'() {
        when:
        def project = new ProjectBuilder().build()
        then:
        project.plugins.apply('python')
        project.plugins.apply('python')
    }

    def 'applied plugin has configurations'() {
        when:
        def project = new ProjectBuilder().build()
        project.plugins.apply('python')
        then:
        for (c in [PythonPlugin.CONFIGURATION_DEFAULT,
                   PythonPlugin.CONFIGURATION_PYTHON,
                   PythonPlugin.CONFIGURATION_WHEEL,
                   PythonPlugin.CONFIGURATION_VENV]) {
            assert project.configurations.getByName(c)
        }
        assert (project.configurations.getByName(PythonPlugin.CONFIGURATION_DEFAULT).getExtendsFrom()*.name == [PythonPlugin.CONFIGURATION_PYTHON])
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
        project.plugins.apply('python')
        then:
        project.getExtensions().getByType(PythonExtension).getDetails().pythonVersion.pythonVersion
    }

    def 'install project has the root file in the collection'() {
        setup:
        temporaryFolder.create()
        def projectDir = temporaryFolder.newFolder('projectDir')

        when:
        def project = new ProjectBuilder().withName('project').withProjectDir(projectDir).build()
        project.plugins.apply('python')

        then:
        PipInstallTask install = (PipInstallTask) project.tasks.getByName(PythonPlugin.TASK_INSTALL_PROJECT)
        install.installFileCollection.getFiles().size() == 1
        install.installFileCollection.getFiles().first().getName() == projectDir.getName()
    }
}
