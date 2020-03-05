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
package com.linkedin.python.importer.deps

import groovy.transform.InheritConstructors
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

@InheritConstructors
class TestDependencyDownloader extends DependencyDownloader {
    @Override
    String downloadDependency(
            String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient) {
        return "Successfully download dependency in unittest scenario!"
    }
}

class DependencyDownloaderTest extends Specification {
    @Rule TemporaryFolder tempDir = new TemporaryFolder()
    File testIvyRepoRoot

    def setup() {
        testIvyRepoRoot = tempDir.newFolder("ivy-repo")
    }

    def "constructor adds project to dependencies set"() {
        given:
        String testProject = "targetProject:1.1.1"
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        Set<String> testProcessedDependencies = new HashSet<>()

        when:
        TestDependencyDownloader dependencyDownloader =
            new TestDependencyDownloader(
                testProject,
                testIvyRepoRoot,
                testDependencySubstitution,
                testProcessedDependencies)

        then:
        assert dependencyDownloader.dependencies.contains(testProject)
    }

    def "download all the dependencies"() {
        given:
        String testProject = "targetProject:1.1.1"
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        Set<String> testProcessedDependencies = new HashSet<>()
        boolean testLatestVersions = true
        boolean testAllowPreReleases = false
        boolean testFetchExtras = false
        boolean testLenient = true
        TestDependencyDownloader dependencyDownloader =
            new TestDependencyDownloader(
                testProject,
                testIvyRepoRoot,
                testDependencySubstitution,
                testProcessedDependencies,
            )

        when:
        dependencyDownloader.download(testLatestVersions, testAllowPreReleases, testFetchExtras, testLenient)

        then:
        assert dependencyDownloader.dependencies.isEmpty()
        assert dependencyDownloader.processedDependencies.contains(testProject)
    }

    def "get module name from filename"() {
        given:
        String testSdistFilename
        String actualModuleName
        String expectedModuleName

        when:
        testSdistFilename = "zc.buildout-2.12.1.tar.gz"
        actualModuleName = DependencyDownloader.getModuleNameFromFilename(testSdistFilename, "2.12.1")
        expectedModuleName = "zc.buildout"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "google-api-python-client-1.7.3.tar.gz"
        actualModuleName = DependencyDownloader.getModuleNameFromFilename(testSdistFilename, "1.7.3")
        expectedModuleName = "google-api-python-client"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "sphinx_rtd_theme-0.4.0-py2.py3-none-any.whl"
        actualModuleName = DependencyDownloader.getModuleNameFromFilename(testSdistFilename, "0.4.0")
        expectedModuleName = "sphinx_rtd_theme"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "psycopg2_binary-2.7.5-cp34-cp34m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl"
        actualModuleName = DependencyDownloader.getModuleNameFromFilename(testSdistFilename, "2.7.5")
        expectedModuleName = "psycopg2_binary"

        then:
        assert actualModuleName == expectedModuleName
    }

    def "build filename by module name"() {
        given:
        String testSdistFilename
        String moduleName
        String filename
        String expectedFilename

        when:
        moduleName = "zc.buildout"
        testSdistFilename = "zc.buildout-2.12.1.tar.gz"
        filename = DependencyDownloader.buildFilenameByModuleName(moduleName, testSdistFilename, "2.12.1")
        expectedFilename = "zc.buildout-2.12.1.tar.gz"
        then:
        filename == expectedFilename

        when:
        moduleName = "google-api-python-client"
        testSdistFilename = "google-api-python-client-1.7.3.tar.gz"
        filename = DependencyDownloader.buildFilenameByModuleName(moduleName, testSdistFilename, "1.7.3")
        expectedFilename = "google-api-python-client-1.7.3.tar.gz"
        then:
        filename == expectedFilename

        when:
        moduleName = "sphinx-rtd-theme"
        testSdistFilename = "sphinx_rtd_theme-0.4.0-py2.py3-none-any.whl"
        filename = DependencyDownloader.buildFilenameByModuleName(moduleName, testSdistFilename, "0.4.0")
        expectedFilename = "sphinx-rtd-theme-0.4.0-py2.py3-none-any.whl"
        then:
        filename == expectedFilename

        when:
        moduleName = "psycopg2-binary"
        testSdistFilename = "psycopg2_binary-2.7.5-cp34-cp34m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl"
        filename = DependencyDownloader.buildFilenameByModuleName(moduleName, testSdistFilename, "2.7.5")
        expectedFilename = "psycopg2-binary-2.7.5-cp34-cp34m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl"
        then:
        filename == expectedFilename

        when:
        moduleName = "typed_ast"
        testSdistFilename = "typed-ast-1.3.2.tar.gz"
        filename = DependencyDownloader.buildFilenameByModuleName(moduleName, testSdistFilename, "1.3.2")
        expectedFilename = "typed_ast-1.3.2.tar.gz"

        then:
        assert filename == expectedFilename
    }
}
