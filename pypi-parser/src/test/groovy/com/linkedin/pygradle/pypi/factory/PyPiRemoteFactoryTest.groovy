package com.linkedin.pygradle.pypi.factory

import spock.lang.Specification


class PyPiRemoteFactoryTest extends Specification {

    def 'make compiler warning go away'() {
        when:
        PyPiRemoteFactory.buildPyPiRemote()
        PyPiRemoteFactory.buildPyPiRemote(PyPiRemoteFactory.PYPI_URL)

        then:
        noExceptionThrown()
    }
}
