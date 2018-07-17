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
package com.linkedin.gradle.python.tasks.action

import com.linkedin.gradle.python.exception.PipExecutionException
import com.linkedin.gradle.python.extension.PythonDetailsTestDouble
import com.linkedin.gradle.python.extension.VirtualEnvironment
import com.linkedin.gradle.python.tasks.action.pip.PipActionHelpers
import com.linkedin.gradle.python.tasks.action.pip.PipInstallAction
import com.linkedin.gradle.python.tasks.exec.ExternalExecTestDouble
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger
import com.linkedin.gradle.python.util.PackageSettings
import com.linkedin.gradle.python.wheel.EmptyWheelCache
import org.gradle.process.ExecSpec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.linkedin.gradle.python.tasks.action.pip.PipActionHelpers.packageInGradleCache

class PipInstallActionTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "pip install uses environment"() {
        setup:
        def override = [foo: ['CPPFLAGS': '-I/some/custom/path/include',
                              'LDFLAGS' : '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib']
        ]
        def settings = new PipActionHelpers.EnvOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def pipInstallAction = createPipInstallAction(settings, execSpec)

        when:
        pipInstallAction.installPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then:
        1 * execSpec.environment(['CPPFLAGS': '-I/some/custom/path/include', 'LDFLAGS': '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib'])
    }

    def 'pip install uses global options'() {
        Map<String, List<String>> override = ['setuptools': ['--global-option', '--dummy-global-option']]
        def settings = new PipActionHelpers.GlobalOptionOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def pipInstallAction = createPipInstallAction(settings, execSpec)

        when:
        pipInstallAction.installPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then:
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--global-option')
            assert idx != -1
            assert it[2] == 'install'
            assert it[idx + 1] == '--dummy-global-option'
        }
    }

    def 'pip install uses install options'() {
        Map<String, List<String>> override = ['setuptools': ['--install-option', '--ignore=E123,E234']]
        def settings = new PipActionHelpers.InstallOptionOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def pipInstallAction = createPipInstallAction(settings, execSpec)

        when:
        pipInstallAction.installPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then:
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--install-option')
            assert idx != -1
            assert it[2] == 'install'
            assert it[idx + 1] == '--ignore=E123,E234'
        }
    }

    def "pip install uses supported language versions"() {
        Map<String, List<String>> override = ['setuptools': ['2.8']]
        def settings = new PipActionHelpers.SupportedLanguageVersionOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def pipInstallAction = createPipInstallAction(settings, execSpec)

        when:
        pipInstallAction.installPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then:
        def e = thrown(PipExecutionException)
        e.message == 'Package setuptools works only with Python versions: [2.8]'
    }

    def 'pip install requires source rebuild'() {
        List<String> override = ['pyflakes']
        def settings = new PipActionHelpers.RequiresRebuildOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def pipInstallAction = createPipInstallAction(settings, execSpec)

        when:
        pipInstallAction.installPackage(packageInGradleCache("pyflakes-1.0.0.tar.gz"), [])

        then:
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--no-deps')
            assert idx != -1
            assert it[2] == 'install'
            assert it[idx + 1] == '--ignore-installed'
        }
    }

    private PipInstallAction createPipInstallAction(PackageSettings settings, ExecSpec execSpec) {
        def project = new ProjectBuilder().withProjectDir(temporaryFolder.root).build()
        def binDir = temporaryFolder.newFolder('build', 'venv', VirtualEnvironment.getPythonApplicationDirectory())
        VirtualEnvironment.findExecutable(binDir.toPath(), "pip").toFile().createNewFile()
        VirtualEnvironment.findExecutable(binDir.toPath(), "python").toFile().createNewFile()
        def details = new PythonDetailsTestDouble(project, binDir.parentFile)
        return new PipInstallAction(settings, project, new ExternalExecTestDouble(execSpec),
            ['CPPFLAGS': 'bogus', 'LDFLAGS': 'bogus'],
            details, new EmptyWheelCache(), new DefaultEnvironmentMerger())
    }
}
