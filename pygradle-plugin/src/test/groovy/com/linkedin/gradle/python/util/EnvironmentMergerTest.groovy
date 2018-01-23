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
package com.linkedin.gradle.python.util

import spock.lang.Specification


/**
 * Unit tests for environment merger implementers.
 */
class EnvironmentMergerTest extends Specification {
    EnvironmentMerger merger = new DefaultEnvironmentMerger()

    def "merge a list of environments"() {
        when: "we use a default merger to merge a list of environments"
        Map<String, String> mergedEnv = merger.mergeEnvironments([['A': '1'], ['B': '2'], ['C': '3']])

        then: "we get merged environment back"
        mergedEnv == ['A': '1', 'B': '2', 'C': '3']
    }

    def "merge into a master environment"() {
        when: "we use a default merger to merge a list of environments into a master environment"
        Map<String, String> masterEnv = ['X': '10', 'Y': '20', 'A': '5']
        Map<String, String> mergedEnv = merger.mergeEnvironments([masterEnv, ['A': '1'], ['A': '2'], ['A': '3']])

        then: "we get merged environments with keys overriding in order of appearance with last winning"
        mergedEnv == ['A': '3', 'X': '10', 'Y': '20']
    }

    def "merge one environment into another"() {
        when: "we use a default merger to merge a source environment into a target environment"
        Map<String, String> target = ['A': '1', 'B': '2']
        merger.mergeIntoEnvironment(target, ['A': '10', 'C': '3'])

        then: "we get merged environments with keys overriding target from source"
        target == ['A': '10', 'B': '2', 'C': '3']
    }

}
