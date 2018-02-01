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

import com.linkedin.gradle.python.extension.PlatformTag
import com.linkedin.gradle.python.extension.PythonVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static com.linkedin.gradle.python.extension.PythonTag.C_PYTHON

class WheelCacheTest extends Specification {

    static final PythonVersion PYTHON_33 = version('3.3')
    static final PythonVersion PYTHON_35 = version('3.5')
    static final PythonVersion PYTHON_27 = version('2.7')

    @Rule
    TemporaryFolder temporaryFolder

    static final PlatformTag MAC_TAG = new PlatformTag('macosx_10_10_intel')
    static final PlatformTag LINUX_TAG = new PlatformTag('linux_x86_64')

    @Unroll
    def 'will find wheels #wheelName == #pythonVersion'() {
        given:
        temporaryFolder.newFile('lib-1.2-' + wheelName + ".whl")

        def cache = new WheelCache(temporaryFolder.root, pythonTag, platformTag)

        expect:
        println temporaryFolder.root.listFiles()
        cache.findWheel('lib', '1.2', pythonVersion).isPresent() == matches

        where:
        wheelName                       | pythonVersion | pythonTag | platformTag | matches
        'cp33-cp33m-linux_x86_64'       | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp33-abi3-linux_x86_64'        | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp3-abi3-linux_x86_64'         | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp33-none-linux_x86_64'        | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp3-none-linux_x86_64'         | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'py33-none-linux_x86_64'        | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'py3-none-linux_x86_64'         | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp33-cp33m-linux_x86_64'       | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'cp33-abi3-linux_x86_64'        | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'cp3-abi3-linux_x86_64'         | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'cp33-none-linux_x86_64'        | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'cp3-none-linux_x86_64'         | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'py33-none-linux_x86_64'        | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'py3-none-linux_x86_64'         | PYTHON_33     | C_PYTHON  | MAC_TAG     | false
        'cp33-cp33m-macosx_10_10_intel' | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'cp33-abi3-macosx_10_10_intel'  | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'cp3-abi3-macosx_10_10_intel'   | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'cp33-none-macosx_10_10_intel'  | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'cp3-none-macosx_10_10_intel'   | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'py33-none-macosx_10_10_intel'  | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'py3-none-macosx_10_10_intel'   | PYTHON_33     | C_PYTHON  | MAC_TAG     | true
        'cp33-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'cp3-none-any'                  | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'py33-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'py3-none-any'                  | PYTHON_33     | C_PYTHON  | LINUX_TAG   | true
        'py32-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py31-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py30-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'cp27-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'cp2-none-any'                  | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py27-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py2-none-any'                  | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py25-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py26-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'py27-none-any'                 | PYTHON_33     | C_PYTHON  | LINUX_TAG   | false
        'cp35-cp35m-macosx_10_10_intel' | PYTHON_35     | C_PYTHON  | MAC_TAG     | true
        'py3-none-any'                  | PYTHON_35     | C_PYTHON  | MAC_TAG     | true
        'cp27-cp33m-linux_x86_64'       | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp27-abi3-linux_x86_64'        | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp2-abi3-linux_x86_64'         | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp27-none-linux_x86_64'        | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp2-none-linux_x86_64'         | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'py27-none-linux_x86_64'        | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'py2-none-linux_x86_64'         | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp27-cp33m-linux_x86_64'       | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'cp27-abi3-linux_x86_64'        | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'cp2-abi3-linux_x86_64'         | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'cp27-none-linux_x86_64'        | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'cp2-none-linux_x86_64'         | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'py27-none-linux_x86_64'        | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'py2-none-linux_x86_64'         | PYTHON_27     | C_PYTHON  | MAC_TAG     | false
        'cp27-cp33m-macosx_10_10_intel' | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'cp27-abi3-macosx_10_10_intel'  | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'cp2-abi3-macosx_10_10_intel'   | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'cp27-none-macosx_10_10_intel'  | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'cp2-none-macosx_10_10_intel'   | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'py27-none-macosx_10_10_intel'  | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'py2-none-macosx_10_10_intel'   | PYTHON_27     | C_PYTHON  | MAC_TAG     | true
        'cp27-none-any'                 | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'cp2-none-any'                  | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'py27-none-any'                 | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'py2-none-any'                  | PYTHON_27     | C_PYTHON  | LINUX_TAG   | true
        'py22-none-any'                 | PYTHON_27     | C_PYTHON  | LINUX_TAG   | false
        'py21-none-any'                 | PYTHON_27     | C_PYTHON  | LINUX_TAG   | false
        'py20-none-any'                 | PYTHON_27     | C_PYTHON  | LINUX_TAG   | false
        'cp36-cp36m-linux_x86_64'       | PYTHON_35     | C_PYTHON  | LINUX_TAG   | false
    }

    static PythonVersion version(String version) {
        return new PythonVersion(version)
    }
}
