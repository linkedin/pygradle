package com.linkedin.python.importer.deps

import com.linkedin.python.importer.pypi.ProjectDetails
import com.linkedin.python.importer.pypi.PypiApiCache
import groovy.json.JsonSlurper
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when
import static org.mockito.Mockito.mock

class SdistDownloaderTest extends Specification {
    @Rule TemporaryFolder tempDir = new TemporaryFolder()
    File testIvyRepoRoot

    def setup() {
        testIvyRepoRoot = tempDir.newFolder("ivy-repo")
    }

//    def "download dependency in Sdist format"() {
//        when:
//        String testProject = "WMI:1.4.8"
//        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
//        Set<String> testProcessedDependencies = new HashSet<>()
//        boolean testLatestVersions = true
//        boolean testAllowPreReleases = false
//        boolean testLenient = true
//        SdistDownloader testSdistDownloader =
//            new SdistDownloader(
//                testProject,
//                testIvyRepoRoot,
//                testDependencySubstitution,
//                testProcessedDependencies,
//                testLatestVersions,
//                testAllowPreReleases,
//                testLenient
//            )
//        SdistDownloader spyTestSdistDownloader = spy(testSdistDownloader)
//
//        File wmiJsonFile = new File(this.getClass().getResource("/deps/WMI.json").toURI())
//        Map<String, Object> wmiJson = (Map<String, Object>) new JsonSlurper().parseText(wmiJsonFile.text)
//        spyTestSdistDownloader.cache = mock(PypiApiCache.class)
//        when(spyTestSdistDownloader.cache.getDetails(testProject, testLenient)).thenReturn((new ProjectDetails(wmiJson)))
//        File wmiSdist = new File(this.getClass().getResource("/deps/WMI-1.4.8.zip").toURI())
//        String uri = "https://files.pythonhosted.org/packages/f7/50/082055d3a6fe0afdfa23ad142c15fc91c016b6039395f160b4301b905766/WMI-1.4.8.zip"
//        when(spyTestSdistDownloader.downloadArtifact(testIvyRepoRoot, uri)).thenReturn(wmiSdist)
//        spyTestSdistDownloader.downloadDependency(testProject)
//
//        then:
//        spyTestSdistDownloader.dependencies.isEmpty()
//
//    }
}
