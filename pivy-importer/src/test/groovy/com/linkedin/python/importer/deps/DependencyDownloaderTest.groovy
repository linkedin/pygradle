package com.linkedin.python.importer.deps

import groovy.transform.InheritConstructors
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.junit.Assert.assertTrue

@InheritConstructors
class TestSubclassDependencyDownloader extends DependencyDownloader {
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
        when:
        String testProject = "targetProject:1.1.1"
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        Set<String> testProcessedDependencies = new HashSet<>()
        boolean testLatestVersions = true
        boolean testAllowPreReleases = false
        boolean testLenient = true
        TestSubclassDependencyDownloader dependencyDownloader =
            new TestSubclassDependencyDownloader(
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
        when:
        String testProject = "targetProject:1.1.1"
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        Set<String> testProcessedDependencies = new HashSet<>()
        boolean testLatestVersions = true
        boolean testAllowPreReleases = false
        boolean testLenient = true
        TestSubclassDependencyDownloader dependencyDownloader =
            new TestSubclassDependencyDownloader(
                testProject,
                testIvyRepoRoot,
                testDependencySubstitution,
                testProcessedDependencies,
                testLatestVersions,
                testAllowPreReleases,
                testLenient
            )
        dependencyDownloader.download()

        then:
        assertTrue(dependencyDownloader.dependencies.isEmpty())
        assertTrue(dependencyDownloader.processedDependencies.contains(testProject))
    }
}
