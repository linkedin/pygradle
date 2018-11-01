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

import com.linkedin.python.importer.distribution.SourceDistPackage
import com.linkedin.python.importer.ivy.IvyFileWriter
import groovy.util.logging.Slf4j
import java.nio.file.Paths

@Slf4j
class SdistDownloader extends DependencyDownloader {
    static final String SOURCE_DIST_PACKAGE_TYPE = "sdist"
    static final String SOURCE_DIST_ORG = "pypi"

    SdistDownloader(
        String project,
        File ivyRepoRoot,
        DependencySubstitution dependencySubstitution,
        Set<String> processedDependencies) {

        super(project, ivyRepoRoot, dependencySubstitution, processedDependencies)
    }

    @Override
    def downloadDependency(
        String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient) {

        def (String name, String version) = dep.split(":")

        def projectDetails = cache.getDetails(name, lenient)
        // project name is illegal, which means we can't find any information about this project on PyPI
        if (projectDetails == null) {
            return
        }

        version = projectDetails.maybeFixVersion(version)
        def sdistDetails = projectDetails.findVersion(version).find { it.packageType == SOURCE_DIST_PACKAGE_TYPE }

        if (sdistDetails == null) {
            if (lenient) {
                log.error("Unable to find source dist for $dep")
                return
            }
            throw new RuntimeException("Unable to find source dist for $dep")
        }

        // make sure the module name has the right letter case and dash or underscore as PyPI
        name = getActualModuleNameFromFilename(sdistDetails.filename, version)
        log.info("Pulling in $name:$version")

        def destDir = Paths.get(ivyRepoRoot.absolutePath, SOURCE_DIST_ORG, name, version).toFile()
        destDir.mkdirs()

        def sdistArtifact = downloadArtifact(destDir, sdistDetails.url)
        def packageDependencies = new SourceDistPackage(name, version, sdistArtifact, cache, dependencySubstitution)
            .getDependencies(latestVersions, allowPreReleases, fetchExtras, lenient)

        new IvyFileWriter(name, version, SOURCE_DIST_PACKAGE_TYPE, [sdistDetails])
            .writeIvyFile(destDir, packageDependencies)

        packageDependencies.each { key, value ->
            dependencies.addAll(value)
        }
    }
}
