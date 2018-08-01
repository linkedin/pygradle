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
package com.linkedin.python.importer.ivy

import com.linkedin.python.importer.pypi.VersionEntry
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class IvyFileWriterTest extends Specification {
    @Rule TemporaryFolder tempDir = new TemporaryFolder()
    private File testDirectory
    File testTempFolder

    def setup() {
        testTempFolder = tempDir.newFolder("testTemp")
        testDirectory = new File(getClass().getClassLoader().getResource("deps").getFile())
    }

    def "test writing Ivy file for wheel which has runtime dependencies"() {
        given:
        VersionEntry testVersionEntry = new VersionEntry("testURI", "wheel", "Django-2.0.6-py3-none-any.whl")
        IvyFileWriter testIvyFileWriter =  new IvyFileWriter("Django", "2.0.6", "bdist_wheel", [testVersionEntry])
        Map<String, List<String>> testDepsMap = [:]
        testDepsMap["argon2"] = ["argon2_cffi:18.1.0"]
        testDepsMap["bcrypt"] = ["bcrypt:3.1.4"]
        testDepsMap["default"] = ["pytz:2018.5"]

        when:
        testIvyFileWriter.writeIvyFile(testTempFolder, testDepsMap, "py3-none-any")
        String actualIvyContent = new File(testTempFolder, "Django-2.0.6-py3-none-any.ivy").text.trim()
        String expectedIvyContent = new File(testDirectory, "Django-2.0.6-py3-none-any.ivy").text.trim()

        then:
        actualIvyContent == expectedIvyContent
    }
}
