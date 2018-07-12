package com.linkedin.python.importer.deps

import com.linkedin.python.importer.ivy.IvyFileWriter
import groovy.transform.InheritConstructors
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.junit.Assert.assertTrue

@InheritConstructors
class TestDependencyDownloader extends DependencyDownloader {
    @Override
    String downloadDependency(String dep) {
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
        boolean testLatestVersions = true
        boolean testAllowPreReleases = false
        boolean testLenient = true

        when:
        TestDependencyDownloader dependencyDownloader =
            new TestDependencyDownloader(
                testProject,
                testIvyRepoRoot,
                testDependencySubstitution,
                testProcessedDependencies,
                testLatestVersions,
                testAllowPreReleases,
                testLenient
            )

        then:
        assertTrue(dependencyDownloader.dependencies.contains(testProject))
    }

    def "download all the dependencies"() {
        given:
        String testProject = "targetProject:1.1.1"
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        Set<String> testProcessedDependencies = new HashSet<>()
        boolean testLatestVersions = true
        boolean testAllowPreReleases = false
        boolean testLenient = true
        TestDependencyDownloader dependencyDownloader =
            new TestDependencyDownloader(
                testProject,
                testIvyRepoRoot,
                testDependencySubstitution,
                testProcessedDependencies,
                testLatestVersions,
                testAllowPreReleases,
                testLenient
            )

        when:
        dependencyDownloader.download()

        then:
        assertTrue(dependencyDownloader.dependencies.isEmpty())
        assertTrue(dependencyDownloader.processedDependencies.contains(testProject))
    }

    def "get actual module name from filename"() {
        given:
        String testSdistFilename
        String actualModuleName
        String expectedModuleName

        when:
        testSdistFilename = "zc.buildout-2.12.1.tar.gz"
        actualModuleName = DependencyDownloader.getActualModuleNameFromFilename(testSdistFilename, "2.12.1")
        expectedModuleName = "zc.buildout"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "google-api-python-client-1.7.3.tar.gz"
        actualModuleName = DependencyDownloader.getActualModuleNameFromFilename(testSdistFilename, "1.7.3")
        expectedModuleName = "google-api-python-client"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "sphinx_rtd_theme-0.4.0-py2.py3-none-any.whl"
        actualModuleName = DependencyDownloader.getActualModuleNameFromFilename(testSdistFilename, "0.4.0")
        expectedModuleName = "sphinx_rtd_theme"
        then:
        actualModuleName == expectedModuleName

        when:
        testSdistFilename = "psycopg2_binary-2.7.5-cp34-cp34m-macosx_10_6_intel.macosx_10_9_intel.macosx_10_9_x86_64.macosx_10_10_intel.macosx_10_10_x86_64.whl"
        actualModuleName = DependencyDownloader.getActualModuleNameFromFilename(testSdistFilename, "2.7.5")
        expectedModuleName = "psycopg2_binary"
        then:
        actualModuleName == expectedModuleName
    }
}
