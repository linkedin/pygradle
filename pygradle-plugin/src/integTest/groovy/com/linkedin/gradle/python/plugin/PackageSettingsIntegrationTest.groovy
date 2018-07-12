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

import com.linkedin.gradle.python.plugin.testutils.DefaultProjectLayoutRule
import com.linkedin.gradle.python.plugin.testutils.PyGradleTestBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.Rule
import spock.lang.Specification


class PackageSettingsIntegrationTest extends Specification {

    @Rule
    final DefaultProjectLayoutRule testProjectDir = new DefaultProjectLayoutRule()

    ///////////////////////////////////
    // PipInstallTask specific tests //
    ///////////////////////////////////

    def "pip install uses environment"() {
        given: "package settings for 'foo' have custom environment"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     Map<String, String> getEnvironment(PackageInfo packageInfo) {
        |         return (packageInfo.name != 'foo') ? [:] : [
        |             'CPPFLAGS': '-I/some/custom/path/include',
        |             'LDFLAGS': '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib',
        |             'DUMMY_MAP': '{\\n}',
        |         ]
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        | project.tasks.withType(PipInstallTask) { PipInstallTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project with debug enabled"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('-d', 'build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "we can observe the environment for 'foo' provided a few lines after its install is logged"
        def match = result.output.find(/Installing foo[\s\S]+?Environment for[^\n]+: \{[\s\S]+?\}\s*\n/)
        match != null
        match.findAll('Installing ').size() == 1
        match.contains('CPPFLAGS=-I/some/custom/path/include')
        match.contains('LDFLAGS=-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib')
        result.output.contains('BUILD SUCCESS')
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
    }

    /*
     * We are using setuptools for option testing because it's the first
     * package installed and it will fail very early. This shortens the
     * integration tests as much as possible.
     */

    def "pip install uses global options"() {
        given: "package settings for 'setuptools' have global options"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getGlobalOptions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'setuptools') ? ['--global-option', '--dummy-global-option'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        | project.tasks.withType(PipInstallTask) { PipInstallTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe global options being passed to 'setuptools' and failing because it does not expect them"
        // the global option for setup.py is passed *before* install command
        result.output.find(/setup.py[^\n]+ --dummy-global-option install/)
        result.output.contains('Running setup.py install for setuptools')
        result.output.contains('error: option --dummy-global-option not recognized')
        result.output.contains('BUILD FAILED')
        result.task(':foo:installSetupRequirements').outcome == TaskOutcome.FAILED
    }

    def "pip install uses install options"() {
        given: "package settings for 'setuptools' have install options"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getInstallOptions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'setuptools') ? ['--install-option', '--ignore=E123,E234'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        | project.tasks.withType(PipInstallTask) { PipInstallTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe install options being passed to 'setuptools' and failing because it does not expect them"
        // the install option is passed *after* install command
        result.output.find(/setup.py[^\n]+ install [^\n]+ --ignore=E123,E234/)
        result.output.contains('Running setup.py install for setuptools')
        result.output.contains('error: option --ignore not recognized')
        result.output.contains('BUILD FAILED')
        result.task(':foo:installSetupRequirements').outcome == TaskOutcome.FAILED
    }

    def "pip install uses supported language versions"() {
        given: "package settings for 'setuptools' have supported language versions"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getSupportedLanguageVersions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'setuptools') ? ['2.8'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        | project.tasks.withType(PipInstallTask) { PipInstallTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe supported language version being checked and failing for non-existent version"
        result.output.contains('Package setuptools works only with Python versions: [2.8]')
        result.output.contains('BUILD FAILED')
        result.task(':foo:installSetupRequirements').outcome == TaskOutcome.FAILED
    }

    /*
     * We are using a dependency on pyflakes because it's also a transitive
     * build dependency and will be re-installed as a runtime dependency.
     */

    def "pip install requires source rebuild"() {
        given: "package settings for 'pyflakes' require a source rebuild"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-sdist'
        | }
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     boolean requiresSourceBuild(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'pyflakes') ? true : super.requiresSourceBuild(packageInfo)
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.PipInstallTask
        |
        | project.tasks.withType(PipInstallTask) { PipInstallTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project with info enabled"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('-i', 'build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "we can observe that required source rebuild happens"
        // pyflakes should be installed in build requirements, and then again in runtime requirements
        result.output.findAll(/Installing pyflakes[\s\S]+? --ignore-installed [^\n]+pyflakes/).size() == 2
        result.output.contains('BUILD SUCCESS')
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
    }

    ////////////////////////////////////
    // BuildWheelsTask specific tests //
    ////////////////////////////////////

    // We are using a dependency on pyflakes, just to have a wheel to build.
    def "wheel uses environment"() {
        given: "package settings for 'foo' have custom environment"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | version = '1.0.0'
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     Map<String, String> getEnvironment(PackageInfo packageInfo) {
        |         return (packageInfo.name != 'foo') ? [:] : [
        |             'CPPFLAGS': '-I/some/custom/path/include',
        |             'LDFLAGS': '-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib',
        |             'DUMMY_MAP': '{\\n}',
        |         ]
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.BuildWheelsTask
        |
        | project.tasks.withType(BuildWheelsTask) { BuildWheelsTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project with debug enabled"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('-d', 'build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "we can observe the environment for 'foo' provided a few lines after its build is logged"
        def match = result.output.find(/Installing foo\S* wheel[\s\S]+?Environment for[^\n]+: \{[\s\S]+?\}\s*\n/)
        match != null
        match.findAll('Installing ').size() == 1
        match.contains('CPPFLAGS=-I/some/custom/path/include')
        match.contains('LDFLAGS=-L/some/custom/path/lib -Wl,-rpath,/some/custom/path/lib')
        result.output.contains('BUILD SUCCESS')
        result.task(':foo:buildWheels').outcome == TaskOutcome.SUCCESS
        result.task(':foo:buildProjectWheel').outcome == TaskOutcome.SUCCESS
    }

    def "wheel uses global options"() {
        given: "package settings for 'pyflakes' have global options"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | version = '1.0.0'
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getGlobalOptions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'pyflakes') ? ['--global-option', '--dummy-global-option'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.BuildWheelsTask
        |
        | project.tasks.withType(BuildWheelsTask) { BuildWheelsTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe global options being passed to 'pyflakes' and failing because it does not expect them"
        // the global option for setup.py is passed *before* bdist_wheel command
        result.output.find(/setup.py[^\n]+ --dummy-global-option bdist_wheel/)
        result.output.contains('Failed building wheel for pyflakes')
        result.output.contains('error: option --dummy-global-option not recognized')
        result.output.contains('BUILD FAILED')
        // the build of everything in the virtualenv succeeded
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        // but the wheel build failed
        result.task(':foo:buildWheels').outcome == TaskOutcome.FAILED
    }

    def "wheel uses build options"() {
        given: "package settings for 'pyflakes' have build options"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | version = '1.0.0'
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getBuildOptions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'pyflakes') ? ['--build-option', '--disable-something'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.BuildWheelsTask
        |
        | project.tasks.withType(BuildWheelsTask) { BuildWheelsTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe build options being passed to 'pyflakes' and failing because it does not expect them"
        // the build option is passed *after* bdist_wheel command
        result.output.find(/setup.py[^\n]+ bdist_wheel [^\n]+ --disable-something/)
        result.output.contains('Failed building wheel for pyflakes')
        result.output.contains('error: option --disable-something not recognized')
        result.output.contains('BUILD FAILED')
        // the build of everything in the virtualenv succeeded
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        // but the wheel build failed
        result.task(':foo:buildWheels').outcome == TaskOutcome.FAILED
    }

    def "wheel uses supported language versions"() {
        given: "package settings for 'pyflakes' have supported language versions"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | version = '1.0.0'
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     List<String> getSupportedLanguageVersions(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'pyflakes') ? ['2.8'] : []
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.BuildWheelsTask
        |
        | project.tasks.withType(BuildWheelsTask) { BuildWheelsTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project"
        def result
        try {
            result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('build')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        } catch (UnexpectedBuildFailure buildFailure) {
            // expected to fail
            result = buildFailure.buildResult
        }
        println result.output

        then: "we can observe supported language version being checked and failing for non-existent version"
        result.output.contains('Package pyflakes works only with Python versions: [2.8]')
        result.output.contains('BUILD FAILED')
        // the build of everything in the virtualenv succeeded
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        // but the wheel build failed
        result.task(':foo:buildWheels').outcome == TaskOutcome.FAILED
    }

    def "wheel requires source rebuild"() {
        given: "package settings for 'pyflakes' require source rebuild"
        testProjectDir.buildFile << """\
        | plugins {
        |     id 'com.linkedin.python-pex'
        | }
        |
        | version = '1.0.0'
        |
        | dependencies {
        |     python 'pypi:pyflakes:+'
        | }
        |
        | import com.linkedin.gradle.python.util.DefaultPackageSettings
        | import com.linkedin.gradle.python.util.PackageInfo
        |
        | class DefaultTestPackageSettings extends DefaultPackageSettings {
        |     DefaultTestPackageSettings(File projectDir) { super(projectDir) }
        |
        |     @Override
        |     boolean requiresSourceBuild(PackageInfo packageInfo) {
        |         return (packageInfo.name == 'pyflakes') ? true : super.requiresSourceBuild(packageInfo)
        |     }
        | }
        |
        | import com.linkedin.gradle.python.tasks.BuildWheelsTask
        |
        | project.tasks.withType(BuildWheelsTask) { BuildWheelsTask task ->
        |     task.packageSettings = new DefaultTestPackageSettings(project.projectDir)
        | }
        |
        |${PyGradleTestBuilder.createRepoClosure()}
        """.stripMargin().stripIndent()

        when: "we build a project with info enabled"
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('-i', 'build')
            .withPluginClasspath()
            .withDebug(true)
            .build()
        println result.output

        then: "we can observe that required source rebuild happens"
        // pyflakes wheel should be re-installed after install in build and runtime requirements of virtualenv.
        result.output.findAll(/Installing pyflakes[\s\S]+?pip install [^\n]+pyflakes/).size() == 2
        result.output.find(/Installing pyflakes\S+ wheel[\s\S]+?pip wheel [^\n]+pyflakes/)
        result.output.contains('BUILD SUCCESS')
        result.task(':foo:installProject').outcome == TaskOutcome.SUCCESS
        result.task(':foo:buildWheels').outcome == TaskOutcome.SUCCESS
    }

}
