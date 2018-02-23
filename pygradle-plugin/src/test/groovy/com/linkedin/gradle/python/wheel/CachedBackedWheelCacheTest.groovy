package com.linkedin.gradle.python.wheel

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CachedBackedWheelCacheTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def 'can find Sphinx-1.6.3'() {
        def wheelCache = temporaryFolder.newFolder('wheel-cache')
        def formats = new SupportedWheelFormats()
        def pythonExec = temporaryFolder.newFile('python')
        formats.addSupportedAbi(new AbiDetails(pythonExec, 'py2', 'none', 'any'))
        CachedBackedWheelCache cache = new CachedBackedWheelCache(wheelCache, formats)

        new File(wheelCache, 'Sphinx-1.6.3-py2.py3-none-any.whl').createNewFile()

        expect:
        cache.findWheel('Sphinx', '1.6.3', pythonExec).isPresent()
    }
}
