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
    private File otherCache

    void setup() {
        projectLayerCache = temporaryFolder.newFolder('project-cache')
        hostLayerCache = temporaryFolder.newFolder('host-cache')
        otherCache = temporaryFolder.newFolder('other-cache')

        def virtualEnv = temporaryFolder.newFile('venv')
        pythonExec = new File(virtualEnv, 'bin/python')
        pythonDetails = new DefaultPythonDetails(new ProjectBuilder().build(), virtualEnv)

        def formats = new DefaultPythonAbiContainer()
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))

        cacheMap = [
            (WheelCacheLayer.PROJECT_LAYER): projectLayerCache,
            (WheelCacheLayer.HOST_LAYER): hostLayerCache,
        ]
        cache = new LayeredWheelCache(cacheMap, formats)
    }

    def "can find Sphinx-1.6.3 in host layer"() {
        setup: "put the wheel in host layer only"
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect: "wheel is found in host layer, but not in project layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 in project layer"() {
        setup: "put the wheel in project layer only"
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect: "wheel is in project layer, but not in host layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 in all layers"() {
        setup: "put the wheel in both layers"
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect: "wheel is in both layers"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
    }

    def "can find Sphinx-1.6.3 regardless of layers when in project layer"() {
        setup: "put the wheel in project layer"
        new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect: "wheel is found without the need to specify the layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails).isPresent()
    }

    def "can find Sphinx-1.6.3 regardless of layers when in host layer"() {
        setup: "put the wheel in host layer"
        new File(hostLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect: "wheel is found without the need to specify the layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails).isPresent()
    }

    def "cannot find Sphinx-1.6.3 if not stored in any cache layer"() {
        expect: "wheel is not found in any layer"
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails).isPresent()
    }

    def "can store Sphinx-1.6.3 to host layer from project layer"() {
        setup: "building the wheel in the project layer is the default"
        def wheelFile  = new File(projectLayerCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()

        when: "wheel is stored in host layer"
        cache.storeWheel(wheelFile, WheelCacheLayer.HOST_LAYER)

        then: "wheel is found in host layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "can store Sphinx-1.6.3 to project layer from other cache"() {
        setup: "put the wheel in another cache"
        def wheelFile  = new File(otherCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()

        when: "wheel is stored in project layer only"
        cache.storeWheel(wheelFile, WheelCacheLayer.PROJECT_LAYER)

        then: "wheel is found in project layer, but not in host layer"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "can store Sphinx-1.6.3 to host layer from other cache"() {
        setup: "put the wheel in another cache"
        def wheelFile  = new File(otherCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()

        when: "wheel is stored in host layer only"
        cache.storeWheel(wheelFile, WheelCacheLayer.HOST_LAYER)

        then: "wheel is found in host layer, but not in project layer"
        !cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "can store Sphinx-1.6.3 to both layers"() {
        setup: "put the wheel in another cache"
        def wheelFile  = new File(otherCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()

        when: "wheel is stored without specifying layer"
        cache.storeWheel(wheelFile)

        then: "wheel is found in both layers"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }

    def "stores over already present wheel"() {
        setup: "put the wheel in both caches"
        def wheelFile  = new File(otherCache, 'Sphinx-1.6.3-py2.py3-none-any.whl')
        wheelFile.createNewFile()
        cache.storeWheel(wheelFile)
        def wheelInHostLayer = cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).get()

        when: "wheel is stored from host to project layer without raising an exception"
        cache.storeWheel(wheelInHostLayer, WheelCacheLayer.PROJECT_LAYER)

        then: "wheel is found in both layers"
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.PROJECT_LAYER).isPresent()
        cache.findWheel('Sphinx', '1.6.3', pythonDetails, WheelCacheLayer.HOST_LAYER).isPresent()
    }
}
