package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.PypiApiCache
import spock.lang.Specification

class WheelsPackageTest extends Specification {
    private File testDirectory
    private WheelsPackage testWheelsPackage

    def setup() {
        testDirectory = new File(getClass().getClassLoader().getResource("deps").getFile())
        File testPackageFile = new File(testDirectory, "Django-2.0.6-py3-none-any.whl")
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        PypiApiCache testPypiApiCache = new PypiApiCache()

        testWheelsPackage = new WheelsPackage("Django", "2.0.6", testPackageFile,
            testPypiApiCache, testDependencySubstitution)
    }

    def "test getting runtime requires from metadata Json file"() {
        when:
        def actualResult = testWheelsPackage.getRuntimeRequiresFromMetadataJson()
        String expectedResultString = "[argon2:[argon2-cffi (>=16.1.0)], bcrypt:[bcrypt], default:[pytz]]"
        then:
        actualResult.toString() == expectedResultString
    }

    def "test getting metadata text file content"() {
        when:
        String actualResult = testWheelsPackage.getMetadataText()
        String expectedResult = new File(testDirectory, "Django-2.0.6-py3-none-any-METADATA").getText()
        then:
        actualResult == expectedResult
    }
}
