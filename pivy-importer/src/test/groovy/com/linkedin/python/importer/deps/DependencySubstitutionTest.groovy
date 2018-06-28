package com.linkedin.python.importer.deps

import spock.lang.Specification
import static org.junit.Assert.assertEquals

class DependencySubstitutionTest extends Specification {
    def "replace dependency from replacementMap and forceMap"() {
        Map<String, String> replacementMap = [
            "module1:originalVersion1": "module1:replacedVersion1",
            "module2:originalVersion2": "module2:replacedVersion2",
            "module3:originalVersion3": "module3:replacedVersion3"]

        Map<String, String> forceMap = ["module3": "forceVersion3", "module4": "forceVersion4"]
        DependencySubstitution testDependencySubstitution = new DependencySubstitution(replacementMap, forceMap)

        when:
        String dependency2 = "module2:originalVersion2"
        String result = testDependencySubstitution.maybeReplace(dependency2)
        then:
        assertEquals("module2:replacedVersion2", result)

        when:
        String dependency3 = "module3:originalVersion3"
        result = testDependencySubstitution.maybeReplace(dependency3)
        then:
        assertEquals("module3:forceVersion3", result)

        when:
        String dependency4 = "module4:originalVersion5"
        result = testDependencySubstitution.maybeReplace(dependency4)
        then:
        assertEquals("module4:forceVersion4", result)

        when:
        String dependency5 = "module5:originalVersion5"
        result = testDependencySubstitution.maybeReplace(dependency5)
        then:
        assertEquals("module5:originalVersion5", result)
    }
}
