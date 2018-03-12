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

class FileBackedWheelCacheTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def 'can find Sphinx-1.6.3'() {
        def wheelCache = temporaryFolder.newFolder('wheel-cache')
        def formats = new SupportedWheelFormats()
        def pythonExec = temporaryFolder.newFile('python')
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))
        FileBackedWheelCache cache = new FileBackedWheelCache(wheelCache, formats)

        new File(wheelCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonExec).isPresent()
    }

    def 'will skip version that are excluded'() {
        def wheelCache = temporaryFolder.newFolder('wheel-cache')
        def formats = new SupportedWheelFormats()
        def pythonExec = temporaryFolder.newFile('python')
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))
        FileBackedWheelCache cache = new FileBackedWheelCache(wheelCache, formats)
        cache.addVersionFilter({ it -> true })

        new File(wheelCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        !cache.findWheel('Sphinx', '1.6.3', pythonExec).isPresent()
    }

    def 'will handle null version'() {
        def wheelCache = temporaryFolder.newFolder('wheel-cache')
        def pythonExec = temporaryFolder.newFile('python')
        def formats = new SupportedWheelFormats()
        FileBackedWheelCache cache = new FileBackedWheelCache(wheelCache, formats)


        expect:
        !cache.findWheel('Sphinx', null, pythonExec).isPresent()
    }
}
