/*
 * Copyright 2017 LinkedIn Corp.
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
package com.linkedin.gradle.python.extension

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonDetailsTest extends Specification {

    def project = new ProjectBuilder().build()
    def details = new PythonDetails(project)
    def originalWhitelistedPythonVersions = PythonVersion.whitelistedPythonVersions
    def originalDefaultPython2 = PythonVersion.defaultPython2
    def originalDefaultPython3 = PythonVersion.defaultPython3

    def cleanup() {
        PythonVersion.whitelistedPythonVersions = originalWhitelistedPythonVersions
        PythonVersion.defaultPython2 = originalDefaultPython2
        PythonVersion.defaultPython3 = originalDefaultPython3
    }

    def 'test normalize to acceptable version 3.x'() {
        expect:
        details.normalizePythonVersion('3.5') == '3.5'
    }

    def 'test normalize to acceptable version 2.x'() {
        expect:
        details.normalizePythonVersion('2.7') == '2.7'
    }

    def 'test normalize to default version 3'() {
        expect:
        details.normalizePythonVersion('3') == '3.5'
    }

    def 'test normalize to default version 2'() {
        expect:
        details.normalizePythonVersion('2') == '2.6'
    }

    def 'test normalize to new default version 3'() {
        setup:
        PythonVersion.defaultPython3 = '3.6'

        expect:
        details.normalizePythonVersion('3') == '3.6'
    }

    def 'test normalize to new default version 2'() {
        setup:
        PythonVersion.defaultPython2 = '2.7'

        expect:
        details.normalizePythonVersion('2') == '2.7'
    }

    def 'test normalize to unacceptable version 2.x'() {
        when:
        details.normalizePythonVersion('2.5')

        then:
        thrown(GradleException)
    }

    def 'test normalize to unacceptable version 3.x'() {
        when:
        details.normalizePythonVersion('3.2')

        then:
        thrown(GradleException)
    }

    def 'test normalize to acceptable customized version'() {
        setup:
        PythonVersion.whitelistedPythonVersions = [ '2.7', '3.5', '3.6' ]

        expect:
        details.normalizePythonVersion('3.5') == '3.5'
    }

    def 'test normalize to unacceptable customized version'() {
        setup:
        PythonVersion.whitelistedPythonVersions = [ '2.7', '3.5', '3.6' ]

        when:
        details.normalizePythonVersion('2.6')

        then:
        thrown(GradleException)
    }

    // Most of the work is done by normalizePythonVersion() so we just add
    // these two additional tests to prove that setting the Python version
    // runs through the expected normalization gauntlet.

    // XXX This fails because it tries to find the Python 3.5 interpreter,
    // which doesn't appear to exist in the test environment:
    //
    // com.linkedin.gradle.python.extension.PythonDetailsTest > test set to acceptable version FAILED
    //     com.linkedin.gradle.python.exception.MissingInterpreterException: Unable to find or execute python
    //         at com.linkedin.gradle.python.extension.PythonDetails.updateFromPythonInterpreter(PythonDetails.java:59)
    //         at com.linkedin.gradle.python.extension.PythonDetails.setPythonVersion(PythonDetails.java:142)
    //         at com.linkedin.gradle.python.extension.PythonDetailsTest.test set to acceptable version(PythonDetailsTest.groovy:113)

    // def 'test set to acceptable version'() {
    //     expect:
    //     details.setPythonVersion('3.5')
    // }

    def 'test set to unacceptable version'() {
        when:
        details.setPythonVersion('2.5')

        then:
        thrown(GradleException)
    }
}

