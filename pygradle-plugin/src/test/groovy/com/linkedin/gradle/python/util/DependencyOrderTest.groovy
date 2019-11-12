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

import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedModuleVersion
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import spock.lang.Specification


/**
 * Unit tests for DependencyOrder utility class.
 */
class DependencyOrderTest extends Specification {
    static Configuration configuration
    static ResolvedComponentResult root
    static Set<ResolvedDependencyResult> rootDependencies
    static ResolvedDependencyResult circularBranch
    static Set<ResolvedComponentResult> expectedDependencies, expectedCircularBranch
    static Set<File> configurationFiles, expectedFiles

    /*
     * Construct dependency tree with the index mapping to integers:
     *
     *               root
     *                |
     *        +-------+--------+- - - -+ the branch 4 used for
     *       /        |         \       \    circular dependency
     *      1         2          3       4       test
     *     / \       /          /       / \
     *    11 12     21         31      41 42
     *        |    /  \                   / \
     *       121  211 212                +--422
     *
     * Different types of mocks and stubs need to be constructed
     * for the complex dependency and configuration types.
     */
    def setupSpec() {
        def ids = [1, 11, 12, 121, 2, 21, 211, 212, 3, 31, 4, 41, 42, 422]
        Map<Integer, ModuleVersionIdentifier> id = [:]
        Map<Integer, LinkedHashSet<ResolvedDependencyResult>> dependencies = [:]
        Map<Integer, ResolvedComponentResult> d = [:]
        Map<Integer, ResolvedDependencyResult> r = [:]
        Map<Integer, File> f = [:]
        Map<Integer, ResolvedArtifact> a = [:]

        for (int i : ids) {
            // prepare module-version ids
            id[i] = Mock(ModuleVersionIdentifier)

            // prepare various types dependencies
            dependencies[i] = new LinkedHashSet<ResolvedDependencyResult>()
            d[i] = Stub(ResolvedComponentResult) {
                getId() >> Mock(ComponentIdentifier)
                getDependencies() >> dependencies[i]
                getModuleVersion() >> id[i]
            }
            r[i] = Stub(ResolvedDependencyResult) {
                getSelected() >> d[i]
            }

            // prepare files
            f[i] = new File("f${i}")

            // prepare artifacts
            a[i] = Stub(ResolvedArtifact) {
                getModuleVersion() >> Stub(ResolvedModuleVersion) {
                    getId() >> id[i]
                }
                getFile() >> f[i]
            }
        }

        // build dependency tree
        dependencies[12].add(r[121])
        dependencies[1].addAll([r[11], r[12]])
        dependencies[21].addAll([r[211], r[212]])
        dependencies[2].add(r[21])
        dependencies[3].add(r[31])

        // create circular dependency branch
        dependencies[422].add(r[42])
        dependencies[42].add(r[422])
        dependencies[4].addAll(r[41], r[42])
        circularBranch = r[4]

        // prepare root using only the first three branches with no cycles
        rootDependencies = new LinkedHashSet<ResolvedDependencyResult>([r[1], r[2], r[3]])
        root = Stub(ResolvedComponentResult) {
            getDependencies() >> rootDependencies
        }
        expectedDependencies = new LinkedHashSet<ResolvedComponentResult>(
            [d[11], d[121], d[12], d[1], d[211], d[212], d[21], d[2], d[31], d[3]]
        )
        expectedCircularBranch = new LinkedHashSet<ResolvedComponentResult>([d[41], d[422], d[42], d[4]])

        // prepare configurations
        configurationFiles = new LinkedHashSet<File>(
            [f[1], f[2], f[3], f[11], f[12], f[21], f[31], f[121], f[211], f[212]]
        )
        configuration = Stub(Configuration) {
            getIncoming() >> Stub(ResolvableDependencies) {
                getResolutionResult() >> Stub(ResolutionResult) {
                    it.getRoot() >> root
                }
            }
            getResolvedConfiguration() >> Stub(ResolvedConfiguration) {
                getResolvedArtifacts() >> new LinkedHashSet<ResolvedArtifact>(
                    [a[1], a[2], a[3], a[11], a[12], a[21], a[31], a[121], a[211], a[212]]
                )
            }
            getFiles() >> configurationFiles
        }
        expectedFiles = new LinkedHashSet<File>(
            [f[11], f[121], f[12], f[1], f[211], f[212], f[21], f[2], f[31], f[3]]
        )
    }

    def "postOrderDependencies returns dependencies in post-order"() {
        setup: "create sets"
        def dependencies = new LinkedHashSet<ResolvedComponentResult>()
        def seen = new HashSet<ComponentIdentifier>()

        when: "called with root of depeendency tree"
        DependencyOrder.postOrderDependencies(root, seen, dependencies)

        then: "returns dependencies in post-order"
        // comparing string representations because sets would match even with a wrong order
        dependencies.toString() == expectedDependencies.toString()
    }

    def "postOrderDependencies handles circular dependencies well"() {
        setup: "make the root with circular dependencies"
        def deps = new LinkedHashSet<ResolvedComponentResult>()
        def seen = new HashSet<ComponentIdentifier>()

        // add the 4th branch for circular dependency test
        expectedDependencies.addAll(expectedCircularBranch)
        rootDependencies.add(circularBranch)

        def root = Stub(ResolvedComponentResult) {
            getDependencies() >> rootDependencies
        }

        when: "called with circular dependencies in the tree"
        DependencyOrder.postOrderDependencies(root, seen, deps)

        then: "returns dependencies in post-order cutting the recursion at the circular dependency"
        // comparing string representations because sets would match even with a wrong order
        deps.toString() == expectedDependencies.toString()

        cleanup:
        expectedDependencies.removeAll(expectedCircularBranch)
        rootDependencies.remove(circularBranch)
    }

    def "postOrderDependencies throws an exception for unresolved dependency"() {
        setup: "make the root with unresolved dependency"
        def dependencies = new LinkedHashSet<ResolvedComponentResult>()
        def seen = new HashSet<ComponentIdentifier>()

        // create a set that can take both resolved and unresolved results
        def branches = new LinkedHashSet<? extends DependencyResult>()
        branches.addAll(rootDependencies)
        branches.add(Mock(UnresolvedDependencyResult))

        def root = Stub(ResolvedComponentResult) {
            getDependencies() >> branches
        }

        when: "called with unresolved dependency in the tree"
        DependencyOrder.postOrderDependencies(root, seen, dependencies)

        then: "throws an exception"
        thrown(GradleException)
    }

    def "configurationPostOrderDependencies returns post-order dependencies"() {
        when: "called with configuration"
        def dependencies = DependencyOrder.configurationPostOrderDependencies(configuration)

        then: "dependencies are in post-order"
        // comparing string representations because sets would match even with a wrong order
        dependencies.toString() == expectedDependencies.toString()
    }

    def "configurationPostOrderFiles returns post-order files for dependencies"() {
        when: "called with configuration"
        def files = DependencyOrder.configurationPostOrderFiles(configuration)

        then: "files are in post-order"
        // comparing string representations because sets would match even with a wrong order
        files.toString() == expectedFiles.toString()
    }

    def "configurationPostOrderFiles raises exception for non-matching sets"() {
        setup: "add unknown file to configuration and remove a known one"
        def saved = configurationFiles.last()
        configurationFiles.remove(saved)
        File unknown = new File('fXXX')
        configurationFiles.add(unknown)

        when: "called with non-matching files size configuration"
        DependencyOrder.configurationPostOrderFiles(configuration)

        then: "throws an exception"
        thrown(GradleException)

        cleanup: "remove the unknown file"
        configurationFiles.remove(unknown)
        configurationFiles.add(saved)
    }

    def "configurationPostOrderFiles handles configuration changes"() {
        setup: "add rest.li file to configuration"
        File restli = new File('rest.li')
        configurationFiles.add(restli)

        when: "called with non-matching files size configuration"
        def files = DependencyOrder.configurationPostOrderFiles(configuration)

        then: "files are in post-order and rest.li added"
        // comparing string representations because sets would match even with a wrong order
        files.toString() == expectedFiles.toString()[0..-2] + ', rest.li]'

        cleanup: "remove the unknown file"
        configurationFiles.remove(restli)
    }
}
