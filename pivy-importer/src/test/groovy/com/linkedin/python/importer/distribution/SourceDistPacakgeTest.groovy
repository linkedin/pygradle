package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.PypiApiCache
import spock.lang.Specification

class SourceDistPacakgeTest extends Specification {
    private File testDirectory
    private SourceDistPackage testSourceDistPackage

    def setup() {
        testDirectory = new File(getClass().getClassLoader().getResource("deps").getFile())
        File testPackageFile = new File(testDirectory, "python-dateutil-2.7.3.tar.gz")
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        PypiApiCache testPypiApiCache = new PypiApiCache()

        testSourceDistPackage = new SourceDistPackage("python-dateutil", "2.7.3", testPackageFile,
            testPypiApiCache, testDependencySubstitution)
    }

    def "test parse requires text"() {
        when:
        String actualResult = testSourceDistPackage.getRequiresTextFile()
        String expectedResult = "six>=1.5\n"
        then:
        actualResult == expectedResult
    }
}
