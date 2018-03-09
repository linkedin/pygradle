package com.linkedin.pivy.dependency

import spock.lang.Specification

class DependencyParserTest extends Specification {

    def 'when \'normal\' dependencies listed, they all show up'() {
        when:
        def contents = '''
            |APScheduler
            |qualname
            |amf-client
            |environment
            |utils
            |six
            |statsd'''.stripMargin()
        def dependencies = DependencyParser.parseRequiresTxt(contents)

        then:
        dependencies.size() == 7
        dependencies.keySet() == ['APScheduler', 'qualname', 'amf-client', 'environment',
                                  'utils', 'six', 'statsd'] as Set
    }

    def 'when dependency have versions'() {
        when:
        def contents = '''
            |SQLAlchemy>=0.7.6
            |Mako
            |python-editor>=0.3'''.stripMargin()
        def dependencies = DependencyParser.parseRequiresTxt(contents)

        then:
        dependencies.size() == 3
        dependencies.keySet() == ['SQLAlchemy', 'Mako', 'python-editor'] as Set
    }
}
