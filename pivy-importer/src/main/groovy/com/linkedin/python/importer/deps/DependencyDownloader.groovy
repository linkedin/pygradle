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

import com.linkedin.python.importer.PypiClient
import com.linkedin.python.importer.pypi.cache.ApiCache
import groovy.util.logging.Slf4j

@Slf4j
abstract class DependencyDownloader {
    Queue<String> dependencies = [] as Queue
    PypiClient pypiClient = new PypiClient()
    String project
    File ivyRepoRoot
    DependencySubstitution dependencySubstitution
    Set<String> processedDependencies
    ApiCache cache

    protected DependencyDownloader(
        String project,
        File ivyRepoRoot,
        DependencySubstitution dependencySubstitution,
        Set<String> processedDependencies,
        ApiCache cache) {

        this.project = project
        this.ivyRepoRoot = ivyRepoRoot
        this.dependencySubstitution = dependencySubstitution
        this.processedDependencies = processedDependencies
        this.cache = cache
        dependencies.add(project)
    }

    def download(boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient) {
        while (!dependencies.isEmpty()) {
            def dependency = dependencies.poll()
            if (dependency in processedDependencies) {
                continue
            }
            downloadDependency(dependency, latestVersions, allowPreReleases, fetchExtras, lenient)
            processedDependencies.add(dependency)
        }
    }

    abstract downloadDependency(
        String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient)

    /**
     * Get the actual module name from artifact name, which has the correct letter case.
     * @param filename the filename of artifact
     * @param revision module version
     * @return actual module name, which is from PyPI
     */
    static String getActualModuleNameFromFilename(String filename, String revision) {
        return filename.substring(0, filename.indexOf(revision) - 1)
    }
}
