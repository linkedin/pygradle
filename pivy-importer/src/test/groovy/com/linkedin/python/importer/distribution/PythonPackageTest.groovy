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
import groovy.transform.InheritConstructors
import spock.lang.Specification

@InheritConstructors
class TestPythonPackage extends PythonPackage {
    @Override
    Map<String, List<String>> getDependencies(boolean latestVersions,
                                              boolean allowPreReleases,
                                              boolean fetchExtras,
                                              boolean lenient) {
        return [:]
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
        String actualText = testPythonPackage.explodeZipForTargetEntry(testEntryName).replaceAll("\r\n", "\n")
        String expectedText = new File(testDirectory, "$testFileName").text
        then:
        actualText == expectedText
    }
}
