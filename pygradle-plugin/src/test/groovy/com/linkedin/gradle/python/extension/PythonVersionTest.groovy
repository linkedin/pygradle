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

import spock.lang.Specification
import spock.lang.Unroll


class PythonVersionTest extends Specification {

    @Unroll
    def 'get #a Python version'() {
        expect:
        new PythonVersion(a).getPythonVersion() == a

        where:
        a        || _
        '2.7.11' || _
        '3.5.2'  || _
    }

    @Unroll
    def 'test #a major minor #b'() {
        expect:
        new PythonVersion(a).getPythonMajorMinor() == b

        where:
        a        || b
        '2.7.11' || '2.7'
        '3.5.2'  || '3.5'
    }

    @Unroll
    def 'test #a major #b'() {
        expect:
        new PythonVersion(a).getPythonMajor() == b

        where:
        a        || b
        '2.7.11' || '2'
        '3.5.2'  || '3'
    }

    @Unroll
    def 'test #a minor #b'() {
        expect:
        new PythonVersion(a).getPythonMinor() == b

        where:
        a        || b
        '2.7.11' || '7'
        '3.5.2'  || '5'
    }

    @Unroll
    def 'test #a patch #b'() {
        expect:
        new PythonVersion(a).getPythonPatch() == b

        where:
        a        || b
        '2.7.11' || '11'
        '3.5.2'  || '2'
    }
}
