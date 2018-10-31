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
import spock.lang.Specification
import spock.lang.Unroll


class PythonDefaultVersionsTest extends Specification {
    @Unroll
    def 'accept any version #a by default normalized to #b'() {
        expect:
        new PythonDefaultVersions().normalize(a) == b

        where:
        a       || b
        '1.5.2' || '1.5.2'
        '2'     || '2.7'
        '3'     || '3.7'
    }

    @Unroll
    def 'explicit Python default versions #a #b normalize #c to #d'() {
        expect:
        new PythonDefaultVersions(a, b).normalize(c) == d

        where:
        a     | b     | c   || d
        '2.8' | '3.0' | '2' || '2.8'
        '2.8' | '3.0' | '3' || '3.0'
    }

    @Unroll
    def 'test acceptable #a normalizes to #b'() {
        expect:
        new PythonDefaultVersions(['2.7', '3.5', '3.6']).normalize(a) == b

        where:
        a     || b
        '3.5' || '3.5'
        '2.7' || '2.7'
        '3'   || '3.7'
        '2'   || '2.7'
    }

    @Unroll
    def 'test unacceptable #a'() {
        when:
        new PythonDefaultVersions(['2.7', '3.5', '3.6']).normalize(a)

        then:
        def e = thrown(GradleException)
        e.message == (
            'Python ' + a + ' is not allowed; choose from [2.7, 3.5, 3.6]\n' +
            'See https://github.com/linkedin/pygradle/blob/master/docs/plugins/python.md#default-and-allowed-python-version')

        where:
        a     | _
        '2.5' | _
        '3.2' | _
    }

    @Unroll
    def 'test acceptable #a normalizes to #b with defaults Py2: #c and Py3: #d'() {
        expect:
        new PythonDefaultVersions(c, d, ['2.7', '3.5', '3.6']).normalize(a) == b

        where:
        a   | c     | d     || b
        '3' | '2.7' | '3.6' || '3.6'
        '2' | '2.7' | '3.6' || '2.7'
    }

    @Unroll
    def 'test acceptable #a normalizes to #b with whitelist #c'() {
        expect:
        new PythonDefaultVersions('2.7', '3.5', c).normalize(a) == b

        where:
        a     | c                            || b
        '3.5' | ['2.7', '3.5', '3.6']        || '3.5'
        '3.7' | ['2.7', '3.5', '3.6', '3.7'] || '3.7'
    }

    @Unroll
    def 'test unacceptable #a with whitelist #b'() {
        when:
        new PythonDefaultVersions('2.7', '3.5', b).normalize(a)

        then:
        def e = thrown(GradleException)
        e.message == (
            'Python ' + a + ' is not allowed; choose from ' + b +
            '\nSee https://github.com/linkedin/pygradle/blob/master/docs/plugins/python.md#default-and-allowed-python-version')

        where:
        a     | b
        '2.6' | ['2.7', '3.5', '3.6']
        '3.5' | ['2.7', '3.6']
    }
}
