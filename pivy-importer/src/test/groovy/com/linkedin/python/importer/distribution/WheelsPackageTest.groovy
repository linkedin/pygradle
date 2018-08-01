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
package com.linkedin.python.importer.distribution

import com.linkedin.python.importer.deps.DependencySubstitution
import com.linkedin.python.importer.pypi.PypiApiCache
import spock.lang.Specification

class WheelsPackageTest extends Specification {
    private File testDirectory
    private WheelsPackage testWheelsPackageDjango
    private WheelsPackage testWheelsPackagePywin

    def setup() {
        testDirectory = new File(getClass().getClassLoader().getResource("deps").getFile())
        File testPackageFileDjango = new File(testDirectory, "Django-2.0.6-py3-none-any.whl")
        File testPackageFilePywin32 = new File(testDirectory, "pywin32-223-cp27-cp27m-win_amd64.whl")
        DependencySubstitution testDependencySubstitution = new DependencySubstitution([:], [:])
        PypiApiCache testPypiApiCache = new PypiApiCache()

        testWheelsPackageDjango = new WheelsPackage("Django", "2.0.6", testPackageFileDjango,
            testPypiApiCache, testDependencySubstitution)

        testWheelsPackagePywin = new WheelsPackage("pywin32", "223", testPackageFilePywin32, testPypiApiCache, testDependencySubstitution)
    }

    def "test getting runtime requires from metadata Json file for wheel package which has runtime dependencies"() {
        when:
        def actualResult = testWheelsPackageDjango.getRuntimeRequiresFromMetadataJson()
        String expectedResultString = "[default:[pytz], argon2:[argon2-cffi (>=16.1.0)], bcrypt:[bcrypt]]"
        then:
        actualResult.toString() == expectedResultString
    }

    def "test getting metadata text file content for wheel package which has runtime dependencies"() {
        when:
        String actualResult = testWheelsPackageDjango.getMetadataText()
        String expectedResult = new File(testDirectory, "Django-2.0.6-py3-none-any-METADATA").getText()
        then:
        actualResult == expectedResult
    }

    def "test getting runtime requires from metadata Json file for wheel package which has no runtime dependencies"() {
        when:
        def actualResult = testWheelsPackagePywin.getRuntimeRequiresFromMetadataJson()
        String expectedResultString = "[default:[]]"
        then:
        actualResult.toString() == expectedResultString
    }
}
