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

import com.linkedin.gradle.python.wheel.internal.DefaultPythonAbiContainer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class FileBackedWheelCacheTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    private File wheelCache
    private File pythonExec
    private FileBackedWheelCache cache

    void setup() {
        wheelCache = temporaryFolder.newFolder('wheel-cache')
        pythonExec = temporaryFolder.newFile('python')
        def formats = new DefaultPythonAbiContainer()
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))
        cache = new FileBackedWheelCache(wheelCache, formats)
    }

    def "can find Sphinx-1.6.3"() {
        setup:
        new File(wheelCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonExec).isPresent()
    }

    def "does not accept partially matching version"() {
        setup: "we have a package with the same version prefix but not matching exactly"
        new File(wheelCache, 'my_special_package-0.0.20-py2.py3-none-any.whl').createNewFile()

        expect: "the partial match between 0.0.2 and 0.0.20 does not find the wheel"
        !cache.findWheel('my-special-package', '0.0.2', pythonExec).isPresent()
    }

    def "finds the exact matching version"() {
        setup: "we have an exact and partial match"
        new File(wheelCache, 'my_special_package-0.0.30-py2.py3-none-any.whl').createNewFile()
        new File(wheelCache, 'my_special_package-0.0.3-py2.py3-none-any.whl').createNewFile()

        expect: "we find the wheel"
        cache.findWheel('my-special-package', '0.0.3', pythonExec).isPresent()
        cache.findWheel('my-special-package', '0.0.3', pythonExec)
            .get().toPath().getFileName().toString() == 'my_special_package-0.0.3-py2.py3-none-any.whl'
    }
}
