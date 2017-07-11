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
import spock.lang.Unroll



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

    @Unroll
    def 'test acceptable #a normalizes to #b'() {
        expect:
        details.normalizePythonVersion(a) == b

        where:
        a     || b
        '3.5' || '3.5'
        '2.7' || '2.7'
        '3'   || '3.5'
        '2'   || '2.6'
    }

    @Unroll
    def 'test unacceptable #a'() {
        when:
        details.normalizePythonVersion(a)

        then:
        thrown(GradleException)

        where:
        a     | _
        '2.5' | _
        '3.2' | _
    }

    @Unroll
    def 'test acceptable #a normalizes to #b with defaults Py2: #c and Py3: #d'() {
        setup:
        PythonVersion.defaultPython2 = c
        PythonVersion.defaultPython3 = d

        expect:
        details.normalizePythonVersion(a) == b

        where:
        a   | c     | d     || b
        '3' | '2.7' | '3.6' || '3.6'
        '2' | '2.7' | '3.6' || '2.7'
    }

    @Unroll
    def 'test acceptable #a normalizes to #b with whitelist #c'() {
        setup:
        PythonVersion.whitelistedPythonVersions = c

        expect:
        details.normalizePythonVersion(a) == b

        where:
        a     | c                            || b
        '3.5' | ['2.7', '3.5', '3.6']        || '3.5'
        '3.7' | ['2.7', '3.5', '3.6', '3.7'] || '3.7'
    }

    @Unroll
    def 'test unacceptable #a with whitelist #b'() {
        setup:
        PythonVersion.whitelistedPythonVersions = b

        when:
        details.normalizePythonVersion(a)

        then:
        thrown(GradleException)

        where:
        a     | b
        '2.6' | ['2.7', '3.5', '3.6']
        '3.5' | ['2.7', '3.6']
    }

    /* Most of the work is done by normalizePythonVersion(), and
       setPythonVersion() just calls the former to normalize and validate the
       chosen Python version.  We'd like to be able to completely test
       setPythonVersion() too, but that's currently impossible, since that
       method searches for a Python interpreter on the file system matching
       the selected version.  We can't guarantee that such a Python version
       will exist so we can't test it without perhaps some mocking of
       operatingSystem.findInPath() , which I haven't been able to come up
       with yet.

       It does seem like this one can be tested though.
     */
    def 'test set to unacceptable version'() {
        when:
        details.setPythonVersion('2.5')

        then:
        thrown(GradleException)
    }
}
