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


class PythonDefaultsTest extends Specification {
    @Unroll
    def 'test acceptable #a normalizes to #b'() {
        setup:
        def defaults = new PythonDefaults()

        expect:
        defaults.normalize(a) == b

        where:
        a     || b
        '3.5' || '3.5'
        '2.7' || '2.7'
        '3'   || '3.5'
        '2'   || '2.6'
    }

    @Unroll
    def 'test unacceptable #a'() {
        setup:
        def defaults = new PythonDefaults()

        when:
        defaults.normalize(a)

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
        def defaults = new PythonDefaults(c, d)

        expect:
        defaults.normalize(a) == b

        where:
        a   | c     | d     || b
        '3' | '2.7' | '3.6' || '3.6'
        '2' | '2.7' | '3.6' || '2.7'
    }

    @Unroll
    def 'test acceptable #a normalizes to #b with whitelist #c'() {
        setup:
        def defaults = new PythonDefaults('2.6', '3.5', c)

        expect:
        defaults.normalize(a) == b

        where:
        a     | c                            || b
        '3.5' | ['2.7', '3.5', '3.6']        || '3.5'
        '3.7' | ['2.7', '3.5', '3.6', '3.7'] || '3.7'
    }

    @Unroll
    def 'test unacceptable #a with whitelist #b'() {
        setup:
        def defaults = new PythonDefaults('2.6', '3.5', b)

        when:
        defaults.normalize(a)

        then:
        thrown(GradleException)

        where:
        a     | b
        '2.6' | ['2.7', '3.5', '3.6']
        '3.5' | ['2.7', '3.6']
    }
}
