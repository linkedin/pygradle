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
package com.linkedin.gradle.python.wheel

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SupportedWheelFormatsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def 'will explode the compact keys'() {
        def pythonExe = temporaryFolder.newFile('python')
        def formats = new SupportedWheelFormats()
        formats.addSupportedAbi(new AbiTriple(pythonExe, 'py2', 'none', 'any'))
        formats.addSupportedAbi(new AbiTriple(pythonExe, 'py3', 'cp27mu', 'any'))

        expect:
        formats.matchesSupportedVersion(pythonExe, 'py2.py3', 'none', 'any')
        formats.matchesSupportedVersion(pythonExe, 'py2.py3', 'cp27mu', 'any')
        !formats.matchesSupportedVersion(pythonExe, 'py3', 'none', 'any')
    }

    def 'different pythons will not match'() {
        def python1Exe = temporaryFolder.newFile('python1')
        def python2Exe = temporaryFolder.newFile('python2')
        def formats = new SupportedWheelFormats()
        formats.addSupportedAbi(new AbiTriple(python1Exe, 'py2', 'none', 'any'))
        formats.addSupportedAbi(new AbiTriple(python2Exe, 'py3', 'cp27mu', 'any'))

        expect:
        formats.matchesSupportedVersion(python1Exe, 'py2.py3', 'none', 'any')
        !formats.matchesSupportedVersion(python2Exe, 'py2.py3', 'none', 'any')
    }

}
