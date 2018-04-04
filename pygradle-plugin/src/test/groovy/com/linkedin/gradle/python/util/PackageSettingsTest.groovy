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
package com.linkedin.gradle.python.util

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


/**
 * Unit tests for package settings implementers.
 */
class PackageSettingsTest extends Specification {
    def project = new ProjectBuilder().build()
    def projectPath = project.file(project.getProjectDir())
    PackageSettings<PackageInfo> packageSettings = new DefaultPackageSettings(project.projectDir)

    def "default package settings environment"() {
        expect: "empty environment"
        packageSettings.getEnvironment(packageInGradleCache('flake8-1.2.3.tar.gz')) == [:]
    }

    def "default package settings global options"() {
        expect: "empty global options"
        packageSettings.getGlobalOptions(packageInGradleCache('Sphinx-1.2.3.tar.gz')) == []
    }

    def "default package settings install options"() {
        expect: "empty install options for non-project package that is not a snapshot"
        packageSettings.getInstallOptions(packageInGradleCache('requests-1.2.3.tar.gz')) == []
    }

    def "package settings install options for snapshot"() {
        expect: "install option --ignore-installed for SNAPSHOT packages to enforce re-install"
        packageSettings.getInstallOptions(packageInGradleCache('requests-1.2.3-SNAPSHOT.tar.gz')) == [
            '--ignore-installed']
    }

    def "package settings install options for project snapshot()"() {
        expect: "project snapshot uses --ignore-installed and --editable"
        packageSettings.getInstallOptions(PackageInfo.fromPath(projectPath)) == ['--ignore-installed', '--editable']
    }

    def "default package settings build options"() {
        expect: "empty build options"
        packageSettings.getBuildOptions(packageInGradleCache('numpy-1.2.3.tar.gz')) == []
    }

    def "package settings build options for snapshot"() {
        expect: "empty build options"
        packageSettings.getBuildOptions(packageInGradleCache('scipy-1.2.3-SNAPSHOT.tar.gz')) == []
    }

    def "default package settings configure options"() {
        expect: "empty configure options"
        packageSettings.getConfigureOptions(packageInGradleCache('pytest-1.2.3.tar.gz')) == []
    }

    def "default package settings supported language versions"() {
        expect: "empty supported language versions"
        packageSettings.getSupportedLanguageVersions(PackageInfo.fromPath(projectPath)) == []
    }

    def "default package settings requires source build"() {
        expect: "does not require source build"
        !packageSettings.requiresSourceBuild(packageInGradleCache('requests-1.2.3.tar.gz'))
    }

    def "package settings require source build for snapshot"() {
        expect: "snapshot requires a build"
        packageSettings.requiresSourceBuild(packageInGradleCache('requests-1.2.3-SNAPSHOT.tar.gz'))
    }

    def "package settings requires a rebuild for the current project"() {
        expect: "project requires a rebuild"
        packageSettings.requiresSourceBuild(PackageInfo.fromPath(projectPath))
    }

    static PackageInfo packageInGradleCache(String name) {
        return PackageInfo.fromPath(new File("/foo/.gradle/caches/", name))
    }

}
