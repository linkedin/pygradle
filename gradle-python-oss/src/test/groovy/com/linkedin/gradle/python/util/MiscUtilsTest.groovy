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
package com.linkedin.gradle.python.util

import org.gradle.api.GradleException
import spock.lang.Specification

class MiscUtilsTest extends Specification {

    def 'can parse boring sdist'() {
        when:
        def (name, version) = MiscUtils.packageInfoFromPath('foo-1.0.0.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0'
    }

    def 'can parse snapshot sdist'() {
        when:
        def (name, version) = MiscUtils.packageInfoFromPath('foo-1.0.0-SNAPSHOT.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0-SNAPSHOT'
    }

    def 'can parse a sdist that has "-" characters in the name'() {
        when:
        def (name, version) = MiscUtils.packageInfoFromPath('foo-bar-1.0.0.tar.gz')
        then:
        assert name == 'foo-bar'
        assert version == '1.0.0'
    }

    def 'can parse a sdist that has "-" characters in the version'() {
        when:
        def (name, version) = MiscUtils.packageInfoFromPath('foo-1.0.0-linkedin1.tar.gz')
        then:
        assert name == 'foo'
        assert version == '1.0.0-linkedin1'
    }

    def 'can parse a sdist from a absolute path'() {
        when:
        def (name, version) =
            MiscUtils.packageInfoFromPath('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pex/0.8.5/5802dfe6dde45790e8a3e6f98f4f94219320f904/pex-0.8.5.tar.gz')
        then:
        assert name == 'pex'
        assert version == '0.8.5'
    }

    def 'can not parse a sdist that has an unknown extension'() {
        when:
        MiscUtils.packageInfoFromPath('foo-1.0.0.xxx')
        then:
        thrown(GradleException)
    }

    def 'can find distgradle in build configuration'() {
        expect:
        MiscUtils.inConfiguration('distgradle', [new File('/a/b/c/distgradle-1.0.0.tar.gz')])
        !MiscUtils.inConfiguration('foo', [new File('/a/b/c/pytest-1.0.0.tar.gz')])
    }

    def 'can find complex path in build configuration'() {
        expect:
        MiscUtils.inConfiguration('pip', [
                new File('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pex/0.8.5/5802dfe6dde45790e8a3e6f98f4f94219320f904/pex-0.8.5.tar.gz'),
                new File('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pip/7.0.3/5802dfe6dde45790e8a3e6f98f4f94219320f904/pip-7.0.3.tar.gz')
        ])
    }
}
