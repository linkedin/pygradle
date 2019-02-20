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


import com.linkedin.gradle.python.extension.internal.DefaultPythonDetails
import com.linkedin.gradle.python.wheel.internal.DefaultPythonAbiContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LayeredWheelCacheTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    private File projectLayerCache
    private File hostLayerCache
    private File pythonExec
    private Map<WheelCacheLayer, File> cacheMap
    private DefaultPythonDetails pythonDetails
    private LayeredWheelCache cache

    void setup() {
        projectLayerCache = temporaryFolder.newFolder('project-cache')
        hostLayerCache = temporaryFolder.newFolder('host-cache')

        def virtualEnv = temporaryFolder.newFile('venv')
        pythonExec = new File(virtualEnv, 'bin/python')

        def formats = new DefaultPythonAbiContainer()
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))

        pythonDetails = new DefaultPythonDetails(new ProjectBuilder().build(), virtualEnv)
        cacheMap = [(WheelCacheLayer.PROJECT_LAYER): projectLayerCache, (WheelCacheLayer.HOST_LAYER): hostLayerCache]
        cache = new LayeredWheelCache(cacheMap, formats)
    }

    def "can find Sphinx-1.6.3 in host layer"() {
        setup:
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 in project layer"() {
        setup:
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 in all layers"() {
        setup:
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 despite layers"() {
        setup:
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonDetails).isPresent()
    }

    def "cannot find Sphinx-1.6.3 if not put to any cache layer"() {
        expect:
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails).isPresent()
    }

    def "can find Sphinx-1.6.3 from target folder"() {
        setup:
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheelInLayer('Sphinx', '1.6.3', pythonExec, WheelCacheLayer.HOST_LAYER).isPresent()
        !cache.findWheelInLayer('Sphinx', '1.6.3', pythonExec, WheelCacheLayer.PROJECT_LAYER).isPresent()
    }

    def "can store Sphinx-1.6.3 to target layer"() {
        setup:
        def wheelFile  = new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()
        cache.storeWheel(wheelFile, WheelCacheLayer.HOST_LAYER)

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }
}
