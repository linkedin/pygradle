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

import java.nio.file.Paths

class PackageInfoTest extends Specification {

    def 'can parse boring sdist'() {
        when:
        def packageInfo = packageInGradleCache('foo-1.0.0.tar.gz')
        then:
        assert packageInfo.name == 'foo'
        assert packageInfo.version == '1.0.0'
    }

    def 'can parse snapshot sdist'() {
        when:
        def packageInfo = packageInGradleCache('foo-1.0.0-SNAPSHOT.tar.gz')
        then:
        assert packageInfo.name == 'foo'
        assert packageInfo.version == '1.0.0-SNAPSHOT'
    }

    def 'can parse a sdist that has "-" characters in the packageInfo.name'() {
        when:
        def packageInfo = packageInGradleCache('foo-bar-1.0.0.tar.gz')
        then:
        assert packageInfo.name == 'foo-bar'
        assert packageInfo.version == '1.0.0'
    }

    def 'can parse a sdist that has "-" characters in the packageInfo.version'() {
        when:
        def packageInfo = packageInGradleCache('foo-1.0.0-linkedin1.tar.gz')
        then:
        assert packageInfo.name == 'foo'
        assert packageInfo.version == '1.0.0-linkedin1'
    }

    def 'can parse a sdist from a absolute path'() {
        when:
        def packageInfo = packageInGradleCache('/Users/sholsapp/.gradle/caches/modules-2/files-2.1/pypi/pex/0.8.5/5802dfe6dde45790e8a3e6f98f4f94219320f904/pex-0.8.5.tar.gz')
        then:
        assert packageInfo.name == 'pex'
        assert packageInfo.version == '0.8.5'
    }

    def 'can parse a windows path'() {
        when:
        def packageInfo = PackageInfo.fromPath(new File('Z:\\pygradle\\.gradle\\caches\\build\\ivy-repo\\pypi\\setuptools\\19.1.1\\setuptools-19.1.1.tar.gz'))
        then:
        assert packageInfo.name == 'setuptools'
        assert packageInfo.version == '19.1.1'
    }

    def 'can not parse an sdist that has an unknown extension'() {
        when:
        packageInGradleCache('foo-1.0.0.xxx')
        then:
        thrown(GradleException)
    }

    def 'can parse a wheel distribution'() {
        when:
        def packageInfo = packageInGradleCache('foo-1.0.0-py2-none-any.whl')
        then:
        assert packageInfo.name == 'foo'
        assert packageInfo.version == '1.0.0'
    }

    // TODO: Remove this test completely when we drop the deprecated method.
    def 'still supports deprecated fromPath method'() {
        when:
        def packageInfo = PackageInfo.fromPath('foo-1.0.0.tgz')
        then:
        packageInfo.name == 'foo'
        packageInfo.version == '1.0.0'
    }

    static PackageInfo packageInGradleCache(String name) {
        return PackageInfo.fromPath(Paths.get("foo", ".gradle", "caches", name))
    }

}
