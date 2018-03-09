package com.linkedin.pygradle.pypi.internal.model

import spock.lang.Specification
import spock.lang.Unroll

class VersionComparatorTest extends Specification {

    @Unroll
    def 'compare #real to #to'() {
        def comparator = new VersionComparator()

        expect:
        (comparator.compare(real, to) == 0) == equal

        where:
        real        | to          | equal
        '1.1.post1' | '1.1'       | false
        '1.1.post1' | '1.1.post1' | true
        '1.1.post1' | '1.1.*'     | true
        '1.1a1'     | '1.1'       | false
        '1.1a1'     | '1.1a1'     | true
        '1.1a1'     | '1.1.*'     | true
        '1.1'       | '1.1'       | true
        '1.1'       | '1.1.0'     | true
        '1.1'       | '1.1.dev1'  | false
        '1.1'       | '1.1a1'     | false
        '1.1'       | '1.1.post1' | false
        '1.1'       | '1.1.*'     | true
        '1.1.2'     | '1.1.*'     | true
    }

    @Unroll
    def 'check ordering #to <=> #from'() {
        def comparator = new VersionComparator()

        expect:
        comparator.compare(to, from) == expected

        where:
        to      | from    | expected
        '2.0.0' | '2.1.0' | -1
    }

    @Unroll
    def 'order test #order'() {
        def comparator = new VersionComparator()
        def versions = order.collect { new DefaultPythonPackageVersion(it) }

        when:
        versions.sort(comparator)

        then:
        versions.collect { it.toVersionString() } == expected

        where:
        order                                | expected
        ['1.2', '1.1', '2.0']                | ['1.1', '1.2', '2.0']
        ['2.0.0', '2.1.0', '2.0.1', '2.1.1'] | ['2.0.0', '2.0.1', '2.1.0', '2.1.1']
    }
}
