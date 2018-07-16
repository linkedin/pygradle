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
package com.linkedin.python.importer.deps

import spock.lang.Specification

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
        assert "module2:replacedVersion2" == result

        when:
        String dependency3 = "module3:originalVersion3"
        result = testDependencySubstitution.maybeReplace(dependency3)
        then:
        assert "module3:forceVersion3" == result

        when:
        String dependency4 = "module4:originalVersion5"
        result = testDependencySubstitution.maybeReplace(dependency4)
        then:
        assert "module4:forceVersion4" == result

        when:
        String dependency5 = "module5:originalVersion5"
        result = testDependencySubstitution.maybeReplace(dependency5)
        then:
        assert "module5:originalVersion5" == result
    }
}
