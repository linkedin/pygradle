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
package com.linkedin.gradle.python.util.internal

import com.linkedin.gradle.python.util.OperatingSystem
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Specification


class ExecutablePathUtilsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    @Requires({ OperatingSystem.current() == OperatingSystem.WINDOWS })
    def 'can pre-pend a folder to the search path - windows'() {
        temporaryFolder.newFolder("foo")
        temporaryFolder.newFolder("bar")
        def fooExample = temporaryFolder.newFile('foo/example.exe')
        def barExample = temporaryFolder.newFile('bar/example.exe')

        expect:
        ExecutablePathUtils.getExecutable([fooExample.parentFile, barExample.parentFile], 'example') == fooExample
    }

    @Requires({ OperatingSystem.current() == OperatingSystem.UNIX })
    def 'can pre-pend a folder to the search path - linux'() {
        temporaryFolder.newFolder("foo")
        temporaryFolder.newFolder("bar")
        def fooExample = temporaryFolder.newFile('foo/example.sh')
        def barExample = temporaryFolder.newFile('bar/example.sh')

        expect:
        ExecutablePathUtils.getExecutable([fooExample.parentFile, barExample.parentFile], 'example.sh') == fooExample
    }
}
