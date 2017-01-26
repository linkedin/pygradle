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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.linkedin.gradle.python.util.SystemExecutables.TEST_FILE

class ExecutablePathUtilsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def 'can pre-pend a folder to the search path'() {
        temporaryFolder.newFolder("foo")
        temporaryFolder.newFolder("bar")
        def fooExample = temporaryFolder.newFile('foo/' + TEST_FILE.getExecutable())
        def barExample = temporaryFolder.newFile('bar/' + TEST_FILE.getExecutable())

        expect:
        ExecutablePathUtils.getExecutable([fooExample.parentFile, barExample.parentFile], TEST_FILE) == fooExample
    }
}
