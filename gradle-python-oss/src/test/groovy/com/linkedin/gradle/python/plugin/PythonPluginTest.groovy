package com.linkedin.gradle.python.plugin


import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import com.linkedin.gradle.python.tasks.PipInstallTask

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

    def 'can parse boring sdist'() {
        when:
        def plugin = new PythonPlugin()
        def (name, version) = plugin.packageInfoFromPath('foo-1.0.0.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0'
    }

    def 'can parse snapshot sdist'() {
        when:
        def plugin = new PythonPlugin()
        def (name, version) = plugin.packageInfoFromPath('foo-1.0.0-SNAPSHOT.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0-SNAPSHOT'
    }

    def 'can parse a sdist that has "-" characters in the name'() {
        when:
        def plugin = new PythonPlugin()
        def (name, version) = plugin.packageInfoFromPath('foo-bar-1.0.0.tar.gz')
        then:
        assert name == 'foo-bar'
        assert version == '1.0.0'
    }

    def 'can parse a sdist that has "-" characters in the version'() {
        when:
        def plugin = new PythonPlugin()
        def (name, version) = plugin.packageInfoFromPath('foo-1.0.0-linkedin1.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0-linkedin1'
    }

    def 'can parse a sdist from a absolute path'() {
        when:
        def plugin = new PythonPlugin()
        def (name, version) = plugin.
            packageInfoFromPath('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pex/0.8.5/5802dfe6dde45790e8a3e6f98f4f94219320f904/pex-0.8.5.tar.gz')
        then:
        assert name == 'pex'
        assert version == '0.8.5'
    }

    def 'can not parse a sdist that has an unknown extension'() {
        when:
        def plugin = new PythonPlugin()
        plugin.packageInfoFromPath('foo-1.0.0.xxx')
        then:
        GradleException ex = thrown()
    }

    def 'can find distgradle in build configuration'() {
        when:
        def plugin = new PythonPlugin()
        then:
        assert plugin.inConfiguration('distgradle', [new File('/a/b/c/distgradle-1.0.0.tar.gz')])
        assert !plugin.inConfiguration('foo', [new File('/a/b/c/pytest-1.0.0.tar.gz')])
    }

    def 'can find complex path in build configuration'() {
        when:
        def plugin = new PythonPlugin()
        then:
        assert plugin.inConfiguration('pip', [
            new File('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pex/0.8.5/5802dfe6dde45790e8a3e6f98f4f94219320f904/pex-0.8.5.tar.gz'),
            new File('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pip/7.0.3/5802dfe6dde45790e8a3e6f98f4f94219320f904/pip-7.0.3.tar.gz')
        ])
    }

    def 'can apply li-java'() {
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
        def plugin = new PythonPlugin()
        then:
        assert project.ext.pythonVersion
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
