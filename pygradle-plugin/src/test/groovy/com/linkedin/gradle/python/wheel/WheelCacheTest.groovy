package com.linkedin.gradle.python.wheel

import com.linkedin.gradle.python.extension.PlatformTag
import com.linkedin.gradle.python.extension.PythonTag
import com.linkedin.gradle.python.extension.PythonVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static com.linkedin.gradle.python.extension.PythonTag.CPython

class WheelCacheTest extends Specification {

    public static final PythonVersion PYTHON_33 = version('3.3')
    public static final PythonVersion PYTHON_35 = version('3.5')
    @Rule
    TemporaryFolder temporaryFolder

    static final PlatformTag MAC_TAG = new PlatformTag('macosx_10_10_x86_64')
    static final PlatformTag LINUX_TAG = new PlatformTag('linux_x86_64')

    @Unroll
    def 'will find wheels #wheelName == #pythonVersion'() {
        given:
        temporaryFolder.newFile('lib-1.2-' + wheelName + ".whl")

        def cache = new WheelCache(temporaryFolder.root, pythonTag, platformTag)

        expect:
        cache.findWheel('lib', '1.2', pythonVersion).isPresent() == matches

        where:
        wheelName                        | pythonVersion | pythonTag | platformTag | matches
        'cp33-cp33m-linux_x86_64'        | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp33-abi3-linux_x86_64'         | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp3-abi3-linux_x86_64'          | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp33-none-linux_x86_64'         | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp3-none-linux_x86_64'          | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py33-none-linux_x86_64'         | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py3-none-linux_x86_64'          | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp33-cp33m-linux_x86_64'        | PYTHON_33     | CPython   | MAC_TAG     | false
        'cp33-abi3-linux_x86_64'         | PYTHON_33     | CPython   | MAC_TAG     | false
        'cp3-abi3-linux_x86_64'          | PYTHON_33     | CPython   | MAC_TAG     | false
        'cp33-none-linux_x86_64'         | PYTHON_33     | CPython   | MAC_TAG     | false
        'cp3-none-linux_x86_64'          | PYTHON_33     | CPython   | MAC_TAG     | false
        'py33-none-linux_x86_64'         | PYTHON_33     | CPython   | MAC_TAG     | false
        'py3-none-linux_x86_64'          | PYTHON_33     | CPython   | MAC_TAG     | false
        'cp33-cp33m-macosx_10_10_x86_64' | PYTHON_33     | CPython   | MAC_TAG     | true
        'cp33-abi3-macosx_10_10_x86_64'  | PYTHON_33     | CPython   | MAC_TAG     | true
        'cp3-abi3-macosx_10_10_x86_64'   | PYTHON_33     | CPython   | MAC_TAG     | true
        'cp33-none-macosx_10_10_x86_64'  | PYTHON_33     | CPython   | MAC_TAG     | true
        'cp3-none-macosx_10_10_x86_64'   | PYTHON_33     | CPython   | MAC_TAG     | true
        'py33-none-macosx_10_10_x86_64'  | PYTHON_33     | CPython   | MAC_TAG     | true
        'py3-none-macosx_10_10_x86_64'   | PYTHON_33     | CPython   | MAC_TAG     | true
        'cp33-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp3-none-any'                   | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py33-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py3-none-any'                   | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py32-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py31-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | true
        'py30-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | true
        'cp27-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | false
        'cp2-none-any'                   | PYTHON_33     | CPython   | LINUX_TAG   | false
        'py27-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | false
        'py2-none-any'                   | PYTHON_33     | CPython   | LINUX_TAG   | false
        'py25-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | false
        'py26-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | false
        'py27-none-any'                  | PYTHON_33     | CPython   | LINUX_TAG   | false
        'cp35-cp35m-macosx_10_10_x86_64' | PYTHON_35     | CPython   | MAC_TAG     | true
        'py3-none-any'                   | PYTHON_35     | CPython   | MAC_TAG     | true

    }

    static PythonVersion version(String version) {
        return new PythonVersion(version)
    }
}
