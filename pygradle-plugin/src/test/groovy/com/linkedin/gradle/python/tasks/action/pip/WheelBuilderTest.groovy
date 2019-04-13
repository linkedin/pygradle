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
package com.linkedin.gradle.python.tasks.action.pip

import com.linkedin.gradle.python.exception.PipExecutionException
import com.linkedin.gradle.python.extension.PythonDetailsFactory
import com.linkedin.gradle.python.extension.PythonDetailsTestDouble
import com.linkedin.gradle.python.extension.internal.DefaultVirtualEnvironment
import com.linkedin.gradle.python.tasks.exec.ExternalExec
import com.linkedin.gradle.python.tasks.exec.ExternalExecFailTestDouble
import com.linkedin.gradle.python.tasks.exec.ExternalExecTestDouble
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger
import com.linkedin.gradle.python.util.DefaultPackageSettings
import com.linkedin.gradle.python.util.PackageInfo
import com.linkedin.gradle.python.util.PackageSettings
import com.linkedin.gradle.python.wheel.WheelCache
import com.linkedin.gradle.python.wheel.WheelCacheLayer
import org.gradle.process.ExecSpec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.linkedin.gradle.python.tasks.action.pip.PipActionHelpers.packageInGradleCache


/**
 * Unit tests for WheelBuilder action class.
 */
class WheelBuilderTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "wheel found in project layer is returned"() {
        setup: "return wheel stub from project layer"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >> Optional.of(new File(expected))
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request package that is present in project layer"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "we get the wheel from project layer without any rebuilding"
        assert pkg.toString() == expected
        0 * execSpec._
    }

    def "wheel found in host layer is returned"() {
        setup: "return wheel stub from host layer only"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/host-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >> Optional.empty()
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.of(new File(expected))
            storeWheel(!null, WheelCacheLayer.PROJECT_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request package that is present only in host layer"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "we get the wheel from host layer without any rebuilding and store it into project layer too"
        assert pkg.toString() == expected
        assert storeCounter == 1
        0 * execSpec._
    }

    def "wheel not found in cache is built"() {
        setup: "do not return wheel from cache layers"
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request package that is not in cache"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "wheel is built but if it's not present after that, the source package is returned as fallback"
        assert pkg == packageInGradleCache("foo-1.0.0.tar.gz").getPackageFile()
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    def "wheel build ignores environment when not customized"() {
        setup: "do not return wheel from cache layers on first attempt"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                    Optional.empty(), Optional.of(new File(expected))]
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.empty()
            storeWheel(!null, WheelCacheLayer.HOST_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request package that is not in cache"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "wheel is built excluding PythonEnvironment, then stored into host layer and returned"
        assert pkg.toString() == expected
        assert storeCounter == 1
        1 * execSpec.environment([:])
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    def "wheel build ignores extraArgs not used with pip wheel command"() {
        setup: "do not return wheel from cache layers on first attempt"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                Optional.empty(), Optional.of(new File(expected))]
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request package that is not in cache"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), ["--upgrade", "--ignore-installed"])

        then: "wheel is built and there are no incompatible options"
        assert pkg.toString() == expected
        1 * execSpec.environment([:])
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
            assert !it.any { entry -> entry == '--upgrade' }
            assert !it.any { entry -> entry == '--ignore-installed' }
        }
    }

    def "wheel build adds environment after failed build without customization"() {
        setup: "do not return wheel from cache layers"
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(null, new ExternalExecFailTestDouble(execSpec), stubWheelCache)

        when: "we request package that is not cache"
        wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "wheel is rebuilt without PythonEnvironment, then with the environment, and error thrown if both fail"
        thrown(PipExecutionException)
        1 * execSpec.environment([:])
        1 * execSpec.environment(['CPPFLAGS':'bogus', 'LDFLAGS':'bogus'])
    }

    def "uses environment"() {
        setup:
        def override = [foo: ['CPPFLAGS': '-I/some/custom/path/include',
                              'LDFLAGS' : '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib']
        ]
        def settings = new PipActionHelpers.EnvOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request package that's not in cache"
        wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "wheel is built with the custom environment"
        1 * execSpec.environment([
            'CPPFLAGS': '-I/some/custom/path/include',
            'LDFLAGS': '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib'
        ])
    }

    def 'uses global options'() {
        Map<String, List<String>> override = ['setuptools': ['--global-option', '--dummy-global-option']]
        def settings = new PipActionHelpers.GlobalOptionOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request package that's not in cache"
        wheelBuilder.getPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then: "wheel is built with the global options"
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--global-option')
            assert idx != -1
            assert it[2] == 'wheel'
            assert it[idx + 1] == '--dummy-global-option'
        }
    }

    def 'uses build options'() {
        Map<String, List<String>> override = ['setuptools': ['--build-option', '--disable-something']]
        def settings = new PipActionHelpers.BuildOptionOverridePackageSetting(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request package that's not in cache"
        wheelBuilder.getPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then: "wheel is built with build options"
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--build-option')
            assert idx != -1
            assert it[2] == 'wheel'
            assert it[idx + 1] == '--disable-something'
        }
    }

    def "does not use supported language versions"() {
        Map<String, List<String>> override = ['setuptools': ['2.8']]
        def settings = new PipActionHelpers.SupportedLanguageVersionOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request the package that's not in cache"
        wheelBuilder.getPackage(packageInGradleCache("setuptools-1.0.0.tar.gz"), [])

        then: "the supported versions are ignored; the caller PipInstallAction should throw exception for this!"
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    def 'requires source rebuild'() {
        List<String> override = ['pyflakes']
        def settings = new PipActionHelpers.RequiresRebuildOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >> Optional.of(new File(expected))
            storeWheel(!null, WheelCacheLayer.HOST_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request the package that is in wheel cache"
        wheelBuilder.getPackage(packageInGradleCache("pyflakes-1.6.0.tar.gz"), [])

        then: "we still rebuild it because of the required source build and we do not store it into host layer"
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--no-deps')
            assert idx != -1
            assert it[2] == 'wheel'
            assert !it.any { entry -> entry == '--ignore-installed' }
        }
        assert storeCounter == 0
    }

    def 'does not rebuild if customized but present'() {
        List<String> override = ['pyflakes']
        def settings = new PipActionHelpers.CustomizedOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >> Optional.of(new File(expected))
            storeWheel(!null, WheelCacheLayer.HOST_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request the package that is in project layer"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("pyflakes-1.6.0.tar.gz"), [])

        then: "we do not rebuild it or store it into host layer"
        assert pkg.toString() == expected
        assert storeCounter == 0
        0 * execSpec._
    }

    def 'rebuilds if customized and not in project layer'() {
        List<String> override = ['pyflakes']
        def settings = new PipActionHelpers.CustomizedOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [Optional.empty(), Optional.of(new File(expected))]
            storeWheel(!null, WheelCacheLayer.HOST_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(settings, execSpec, stubWheelCache)

        when: "we request the package that is not in project layer"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("pyflakes-1.6.0.tar.gz"), [])

        then: "we build it and get the wheel, but do not store it into host layer"
        assert pkg.toString() == expected
        assert storeCounter == 0
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--no-deps')
            assert idx != -1
            assert it[2] == 'wheel'
            assert !it.any { entry -> entry == '--ignore-installed' }
        }
    }

    def 'builds project wheel but returns the project itself'() {
        setup: "do not return wheel from cache layers on first attempt"
        def execSpec = Mock(ExecSpec)
        def fakeWheel = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                Optional.empty(), Optional.of(new File(fakeWheel))]
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.empty()
            storeWheel(!null, WheelCacheLayer.HOST_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request project to be built"
        def pkg = wheelBuilder.getPackage(PackageInfo.fromPath(wheelBuilder.project.getProjectDir()), [])

        then: "wheel is built but not stored to host layer and project directory returned for editable install"
        assert pkg.toString() == wheelBuilder.project.getProjectDir().toString()
        assert storeCounter == 0
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    def "wheel with matching custom environment is built"() {
        setup: "return wheel stub from host layer only"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/host-dir/wheel'
        def fakeWheel = 'fake/project-dir/wheel'
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def storeCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                Optional.empty(), Optional.of(new File(fakeWheel))]
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.of(new File(expected))
            storeWheel(!null, WheelCacheLayer.PROJECT_LAYER) >> { storeCounter++ }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache, ['OPENBLAS': 'None'])

        when: "we request package that is present only in host layer but has custom pythonEnvironment"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("numpy-12.0.0.tar.gz"), [])

        then: "we do not get it from the host layer but build the custom wheel instead and get it from project layer"
        assert pkg.toString() == fakeWheel
        assert storeCounter == 0
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    private WheelBuilder createWheelBuilder(PackageSettings settings, ExecSpec execSpec, WheelCache wheelCache) {
        return createWheelBuilder(settings, new ExternalExecTestDouble(execSpec), wheelCache)
    }

    private WheelBuilder createWheelBuilder(ExecSpec execSpec, WheelCache wheelCache) {
        createWheelBuilder(null, new ExternalExecTestDouble(execSpec), wheelCache)
    }

    private WheelBuilder createWheelBuilder(ExecSpec execSpec, WheelCache wheelCache, Map<String, String> environment) {
        createWheelBuilder(null, new ExternalExecTestDouble(execSpec), wheelCache, environment)
    }

    private WheelBuilder createWheelBuilder(PackageSettings settings, ExternalExec executor, WheelCache wheelCache) {
        createWheelBuilder(settings, executor, wheelCache, ['CPPFLAGS': 'bogus', 'LDFLAGS': 'bogus'])
    }

    private WheelBuilder createWheelBuilder(
            PackageSettings settings, ExternalExec executor, WheelCache wheelCache, Map<String, String> environment) {
        def project = new ProjectBuilder().withProjectDir(temporaryFolder.root).build()
        def packageSettings = settings ?: new DefaultPackageSettings(project.getProjectDir())
        def binDir = temporaryFolder.newFolder('build', 'venv', PythonDetailsFactory.getPythonApplicationDirectory())
        DefaultVirtualEnvironment.findExecutable(binDir.toPath(), "pip").createNewFile()
        DefaultVirtualEnvironment.findExecutable(binDir.toPath(), "python").createNewFile()
        def details = new PythonDetailsTestDouble(project, binDir.parentFile)
        return new WheelBuilder(packageSettings, project, executor, environment, details, wheelCache,
                new DefaultEnvironmentMerger(), { it -> false })
    }
}
