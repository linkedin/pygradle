package com.linkedin.pygradle.pypi.internal.service

import groovy.transform.ToString
import groovy.transform.TupleConstructor
import com.linkedin.pygradle.pypi.model.Dependency
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DependencyCondition

@ToString
@TupleConstructor
class ExpectedDependencies {
    String name
    String version
    DependencyOperator operator
    Set<DependencyCondition> extras

    boolean isEqual(com.linkedin.pygradle.pypi.model.Dependency dep) {
        System.out.println("Comparing to $dep")
        if (name != dep.name || version != dep.version.toVersionString() || operator != dep.operator) {
            return false
        }

        if (dep.extras.size() != extras.size()) {
            return false
        }

        dep.extras.forEach { depExtra ->
            if (!extras.contains(depExtra)) {
                return false
            }
        }

        return true
    }
}
