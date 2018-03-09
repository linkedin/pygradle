package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DefaultDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition
import com.linkedin.pygradle.pypi.service.PyPiRemote
import spock.lang.Specification

import static com.linkedin.pygradle.pypi.internal.service.ModelUtil.createPackageDetails


class WheelManifestParserTest extends Specification {
    def 'can parse pytest 3.3.0 requires.txt'() {
        def remote = new PyPiRemoteTestDouble([
            configparser: [name: 'configparser', version: '1.1'],
            enum34      : [name: 'enum34', version: '2.1'],
        ])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/manifest/flake8-3.3.0.metadata.json').text
        def parser = new WheelManifestParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)
        dependencies.each { println "\t - $it" }

        then:
        def expectedDependencies = [
            new ExpectedDependencies('mccabe', '0.6.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('mccabe', '0.7.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.0.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.4.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.5.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.6.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('configparser', '1.1', DependencyOperator.GREATER_THAN_EQUAL, [new SystemDependencyCondition("python", "3.2", DependencyOperator.LESS_THAN)] as Set),
            new ExpectedDependencies('enum34', '2.1', DependencyOperator.GREATER_THAN_EQUAL, [new SystemDependencyCondition("python", "3.4", DependencyOperator.LESS_THAN)] as Set),
        ]

        dependencies.size() == expectedDependencies.size()
        expectedDependencies.each { dep ->
            println "Searching for ------- $dep"
            assert dependencies.find { dep.isEqual(it) }
        }
    }

    def 'can parse pytest 2.6.2 requires.txt'() {
        def remote = new PyPiRemoteTestDouble([:])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/manifest/flake8-2.6.2.metadata.json').text
        def parser = new WheelManifestParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)
        dependencies.each { println "\t - $it" }

        then:
        def expectedDependencies = [
            new ExpectedDependencies('mccabe', '0.2.1', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('mccabe', '0.6', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.1', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.2.0', DependencyOperator.NOT_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '0.8.1', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.3', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.2.1', DependencyOperator.NOT_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.2.2', DependencyOperator.NOT_EQUAL, [new DefaultDependencyCondition()] as Set),
        ]

        dependencies.size() == expectedDependencies.size()
        expectedDependencies.each { dep ->
            println "Finding ---- $dep"
            assert dependencies.find { dep.isEqual(it) }
        }
    }
}
