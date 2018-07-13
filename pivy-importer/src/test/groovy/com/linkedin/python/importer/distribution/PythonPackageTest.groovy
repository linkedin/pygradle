package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.PypiApiCache
import groovy.transform.InheritConstructors
import spock.lang.Specification

@InheritConstructors
class TestPythonPackage extends PythonPackage {
    @Override
    Map<String, List<String>> getDependencies(boolean latestVersions,
                                              boolean allowPreReleases,
                                              boolean lenient) {
        return new HashMap<String, List<String>>()
    }
}

class PythonPackageTest extends Specification {
    private File testDirectory
    private TestPythonPackage testPythonPackage

    def setup() {
        testDirectory = new File(getClass().getClassLoader().getResource("deps").getFile())
        File testPackageFile = new File(testDirectory, "WMI-1.4.8.zip")
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        PypiApiCache testPypiApiCache = new PypiApiCache()

        testPythonPackage = new TestPythonPackage("WMI", "1.4.8", testPackageFile,
            testPypiApiCache, testDependencySubstitution)
    }

    def "test explode zip for target entry"() {
        given:
        String testFileName = "PKG-INFO"
        String testEntryName = "WMI-1.4.8/$testFileName"
        when:
        String actualText = testPythonPackage.explodeZipForTargetEntry(testEntryName).replaceAll(System.lineSeparator(), "\n")
        String expectedText = new File(testDirectory, "$testFileName").text
        then:
        actualText == expectedText
    }
}
