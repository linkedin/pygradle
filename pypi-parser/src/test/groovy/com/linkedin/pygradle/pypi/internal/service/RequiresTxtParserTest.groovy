package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DefaultDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.PackageRequiredDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition
import spock.lang.Specification

class RequiresTxtParserTest extends Specification {

    def 'can parse pytest requires.txt'() {
        def remote = new PyPiRemoteTestDouble([
            colorama  : [name: 'colorama', version: '1.1'],
            setuptools: [name: 'setuptools', version: '2.1'],
            argparse  : [name: 'argparse', version: '3.1'],
        ])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/requires/pytest.requires.txt').text
        def parser = new RequiresTxtParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)

        then:
        dependencies.find {
            it.name == 'py' && it.version.toVersionString() == '1.4.29' && it.extras == [new DefaultDependencyCondition()] as Set
        }
        dependencies.find {
            it.name == 'setuptools' && it.version.toVersionString() == '2.1' && it.extras == [new DefaultDependencyCondition()] as Set
        }
        dependencies.find {
            it.name == 'colorama' && it.version.toVersionString() == '1.1' && it.extras == [new SystemDependencyCondition('sys_platform', 'win32', DependencyOperator.EQUAL)] as Set
        }
        dependencies.find {
            it.name == 'argparse' && it.version.toVersionString() == '3.1' && it.extras == [new SystemDependencyCondition('python_version', '2.6', DependencyOperator.EQUAL)] as Set
        }
    }

    def 'can parse pex requires.txt'() {
        def remote = new PyPiRemoteTestDouble([:])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/requires/pex.requires.txt').text
        def parser = new RequiresTxtParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)

        then:
        [
            ['setuptools', '20.3', DependencyOperator.GREATER_THAN_EQUAL, new DefaultDependencyCondition()],
            ['setuptools', '34.0', DependencyOperator.LESS_THAN, new DefaultDependencyCondition()],
            ['wheel', '0.26.0', DependencyOperator.GREATER_THAN_EQUAL, new DefaultDependencyCondition()],
            ['wheel', '0.30.0', DependencyOperator.LESS_THAN, new DefaultDependencyCondition()],
            ['CacheControl', '0.12.3', DependencyOperator.GREATER_THAN_EQUAL, new PackageRequiredDependencyCondition('cachecontrol')],
            ['requests', '2.8.14', DependencyOperator.GREATER_THAN_EQUAL, new PackageRequiredDependencyCondition('requests')],
            ['subprocess32', '3.2.7', DependencyOperator.GREATER_THAN_EQUAL, new PackageRequiredDependencyCondition('subprocess')],
        ].each { dep ->
            assert dependencies.find {
                it.name == dep[0] && it.version.toVersionString() == dep[1] && it.operator == dep[2] && it.extras == [dep[3]] as Set
            }
        }

        dependencies.size() == 7
    }

    def 'can parse setuptools requires.txt'() {
        def remote = new PyPiRemoteTestDouble([:])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/requires/setuptools.requires.txt').text
        def parser = new RequiresTxtParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)

        then:
        dependencies.find {
            it.name == 'certifi' && it.version.toVersionString() == '2016.9.26' &&
                it.extras == [new PackageRequiredDependencyCondition('certs')] as Set
        }
        dependencies.find {
            it.name == 'wincertstore' && it.version.toVersionString() == '0.2' &&
                it.extras == [new SystemDependencyCondition('sys_platform', 'win32', DependencyOperator.EQUAL)] as Set
        }
    }

    def 'can parse Sphinx requires.txt'() {
        def remote = new PyPiRemoteTestDouble([
            nose      : [name: 'nose', version: '1.2'],
            mock      : [name: 'mock', version: '1.2'],
            simplejson: [name: 'simplejson', version: '1.2'],
            imagesize : [name: 'imagesize', version: '1.2'],
            babel     : [name: 'Babel', version: '1.2']
        ])
        def text = RequiresTxtParserTest.classLoader.getResourceAsStream('sample/requires/Sphinx.requires.txt').text
        def parser = new RequiresTxtParser(remote)

        when:
        def dependencies = parser.calculateDependencies(text)

        then:
        [
            ['Babel', '1.3', DependencyOperator.GREATER_THAN_EQUAL, new DefaultDependencyCondition()],
            ['Babel', '2.0', DependencyOperator.NOT_EQUAL, new DefaultDependencyCondition()],
        ].each { dep ->
            assert dependencies.find {
                it.name == dep[0] && it.version.toVersionString() == dep[1] && it.operator == dep[2] && it.extras == [dep[3]] as Set
            }
        }
    }
}
