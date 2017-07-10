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
package com.linkedin.gradle.python.extension

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonDetailsTest extends Specification {

    def project = new ProjectBuilder().build()
    def details = new PythonDetails(project)
    def savedWhitelistedPythonVersions = PythonVersion.whitelistedPythonVersions

    def cleanup() {
        PythonVersion.whitelistedPythonVersions = savedWhitelistedPythonVersions
    }

    def 'test set to acceptable version'() {
        expect:
        details.setPythonVersion('3.5')
    }

    def 'test set to unacceptable version'() {
        when:
        details.setPythonVersion('2.5')

        then:
        thrown(GradleException)
    }

    def 'test set to customized acceptable version'() {
        setup:
        PythonVersion.whitelistedPythonVersions = [ '2.7', '3.5', '3.6' ]

        expect:
        details.setPythonVersion('3.5')
    }

    def 'test set to customized unacceptable version'() {
        setup:
        PythonVersion.whitelistedPythonVersions = [ '2.7', '3.5', '3.6' ]

        when:
        details.setPythonVersion('2.6')

        then:
        thrown(GradleException)
    }
}

