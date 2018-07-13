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
