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
import org.gradle.api.logging.Logger
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
        pkg.toString() == expected
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
        pkg.toString() == expected
        storeCounter == 1
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
        pkg == packageInGradleCache("foo-1.0.0.tar.gz").getPackageFile()
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
        pkg.toString() == expected
        storeCounter == 1
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
        pkg.toString() == expected
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
        def ready = true
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
            setWheelsReady(!null) >> { boolean r -> ready = r }
        }
        def wheelBuilder = createWheelBuilder(null, new ExternalExecFailTestDouble(execSpec), stubWheelCache)
        def sdistPkg = packageInGradleCache("foo-1.0.0.tar.gz")

        when: "we request package that is not in cache"
        def pkg = wheelBuilder.getPackage(sdistPkg, [])

        then: "wheel is rebuilt without environment, then with it; package returned if both fail; wheel not ready flag"
        1 * execSpec.environment([:])
        1 * execSpec.environment(['CPPFLAGS':'bogus', 'LDFLAGS':'bogus'])
        pkg == sdistPkg.getPackageFile()
        !ready
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
        storeCounter == 0
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
        pkg.toString() == expected
        storeCounter == 0
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
        pkg.toString() == expected
        storeCounter == 0
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            def idx = it.indexOf('--no-deps')
            assert idx != -1
            assert it[2] == 'wheel'
            assert !it.any { entry -> entry == '--ignore-installed' }
        }
    }

    def 'does not build project wheel but returns the project itself'() {
        setup: "do not return wheel from cache layers on first attempt"
        def execSpec = Mock(ExecSpec)
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we request project to be built"
        def pkg = wheelBuilder.getPackage(PackageInfo.fromPath(wheelBuilder.project.getProjectDir()), [])

        then: "wheel is not built and project directory is returned for editable install"
        pkg.toString() == wheelBuilder.project.getProjectDir().toString()
        0 * execSpec._
    }

    def 'builds generated code wheel but getting the project version for it'() {
        setup: "do not return wheel from cache layers on first attempt"
        def execSpec = Mock(ExecSpec)
        def fakeWheel = 'fake/project-dir/wheel'
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                Optional.empty(), Optional.of(new File(fakeWheel))]
            findWheel(!null, !null, !null, WheelCacheLayer.HOST_LAYER) >> Optional.empty()
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)
        def generatedDir = temporaryFolder.newFolder('build', 'generated', wheelBuilder.project.getName())

        when: "we request the generated code directory to be built"
        def pkg = wheelBuilder.getPackage(PackageInfo.fromPath(generatedDir), [])

        then: "wheel is built and returned for install"
        pkg.toString() == fakeWheel
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
        pkg.toString() == fakeWheel
        storeCounter == 0
        1 * execSpec.commandLine(_) >> { List<List<String>> args ->
            println args
            def it = args[0]
            assert it[2] == 'wheel'
        }
    }

    def "update wheel readiness works"() {
        setup: "prepare wheel cache returns to return a wheel once and no wheel second time"
        def execSpec = Mock(ExecSpec)
        def expected = 'fake/project-dir/wheel'
        boolean ready = true
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(!null, !null, !null, WheelCacheLayer.PROJECT_LAYER) >>> [
                Optional.of(new File(expected)), Optional.empty()]
            setWheelsReady(!null) >> { boolean r -> ready = r }
            isWheelsReady() >> { ready }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)
        def sdistPkg = packageInGradleCache("foo-1.0.0.tar.gz")
        def testPkg = PackageInfo.fakeFromOther(sdistPkg, null, null)

        when: "we ask for wheel that's present in the wheel cache"
        wheelBuilder.updateWheelReadiness(sdistPkg)

        then: "the wheel readiness flag is still up"
        ready

        when: "we ask for wheel that's not in the wheel cache"
        wheelBuilder.updateWheelReadiness(packageInGradleCache("foo-2.0.0.tar.gz"))

        then: "the wheel readiness flag would be dropped down"
        !ready

        when: "the package has null name or version the flag gets set up"
        ready = true
        wheelBuilder.updateWheelReadiness(testPkg)

        then: "the wheel readiness flag gets dropped down again without search"
        !ready
    }

    def "logger can be obtained"() {
        setup: "prepare wheel cache mock"
        def execSpec = Mock(ExecSpec)
        def mockWheelCache = Mock(WheelCache)
        def wheelBuilder = createWheelBuilder(execSpec, mockWheelCache)

        when: "we ask for the logger"
        def logger = wheelBuilder.getLogger()

        then: "we get the object of correct type"
        logger instanceof Logger
    }

    /*
     * The tests below cover the code paths that are not normally exercised and require fakes.
     */

    def "empty command line does not build wheel"() {
        setup: "prepare wheel cache to return no target directory on second call"
        def execSpec = Mock(ExecSpec)
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def findCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >>> [
                Optional.of(new File('fake/project-dir')), Optional.empty()]
            findWheel(*_) >> {
                findCounter++
                Optional.empty()
            }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we try to build wheel that's not found in cache layers"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "we attempt to build but do not run empty command line and return the package instead"
        pkg == packageInGradleCache("foo-1.0.0.tar.gz").getPackageFile()
        0 * execSpec._
        // Search should have happened in the project layer, then host layer, and then after attempted build.
        findCounter == 3
    }

    def "empty target directory does not build wheel"() {
        setup: "prepare wheel cache to return no target directory on first call"
        def execSpec = Mock(ExecSpec)
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def findCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.empty()
            findWheel(*_) >> {
                findCounter++
                Optional.empty()
            }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)

        when: "we try to build wheel that's not found in cache layers"
        def pkg = wheelBuilder.getPackage(packageInGradleCache("foo-1.0.0.tar.gz"), [])

        then: "we leave without attempting the search or build"
        pkg == packageInGradleCache("foo-1.0.0.tar.gz").getPackageFile()
        0 * execSpec._
        findCounter == 0
    }

    def "null name or version return package instead of wheel"() {
        setup: "prepare wheel cache and test package info object"
        def execSpec = Mock(ExecSpec)
        // Cardinality of calls on stubs cannot be asserted, as opposed to mocks, so we keep the counter.
        def findCounter = 0
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> {
                findCounter++
                Optional.empty()
            }
        }
        def wheelBuilder = createWheelBuilder(execSpec, stubWheelCache)
        def sdistPkg = packageInGradleCache("foo-1.0.0.tar.gz")
        def testPkg = PackageInfo.fakeFromOther(sdistPkg, null, null)

        when: "we try to build wheel for package with null name and version"
        def pkg = wheelBuilder.getPackage(testPkg, [])

        then: "we leave without attempting the search or build"
        pkg == sdistPkg.getPackageFile()
        0 * execSpec._
        findCounter == 0
    }

    def "failed custom wheel build drops wheel readiness flag"() {
        setup: "do not return wheel from cache layers"
        List<String> override = ['foo']
        def settings = new PipActionHelpers.CustomizedOverridePackageSettings(temporaryFolder, override)
        def execSpec = Mock(ExecSpec)
        def ready = true
        def stubWheelCache = Stub(WheelCache) {
            getTargetDirectory() >> Optional.of(new File('fake/project-dir'))
            findWheel(*_) >> Optional.empty()
            setWheelsReady(!null) >> { boolean r -> ready = r }
        }
        def wheelBuilder = createWheelBuilder(settings, new ExternalExecFailTestDouble(execSpec), stubWheelCache, [:])
        def sdistPkg = packageInGradleCache("foo-1.0.0.tar.gz")

        when: "we request package that is not cache"
        def pkg = wheelBuilder.getPackage(sdistPkg, [])

        then: "wheel is rebuilt without environment, then with it; package returned if both fail; wheel not ready flag"
        1 * execSpec.environment([:])
        pkg == sdistPkg.getPackageFile()
        !ready

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
