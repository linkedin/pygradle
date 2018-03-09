package com.linkedin.pygradle.pypi.internal.parser

import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition
import spock.lang.Specification
import spock.lang.Unroll

class DependencyScopeParserTest extends Specification {

    @Unroll
    def 'system dependency test (#input)'() {

        when:
        def result = DependencyScopeParser.parseScope(input)

        then:
        result.size() == 1
        (result.first() as SystemDependencyCondition).name == name
        (result.first() as SystemDependencyCondition).version == version
        (result.first() as SystemDependencyCondition).condition == condition

        where:
        input                      | name             | version | condition
        "python_version<\"3.2\""   | 'python_version' | '3.2'   | DependencyOperator.LESS_THAN
        ":sys_platform==\"win32\"" | 'sys_platform'   | 'win32' | DependencyOperator.EQUAL
    }
}
