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

import com.linkedin.python.importer.distribution.WheelsPackage
import com.linkedin.python.importer.ivy.IvyFileWriter

import groovy.util.logging.Slf4j
import java.nio.file.Paths

@Slf4j
class WheelsDownloader extends DependencyDownloader {
    static final String BINARY_DIST_PACKAGE_TYPE = "bdist_wheel"
    static final String BINARY_DIST_ORG = "wheel"

    WheelsDownloader(String project, File ivyRepoRoot, DependencySubstitution dependencySubstitution,
                     boolean latestVersions, boolean allowPreReleases, boolean lenient) {

        super(project, ivyRepoRoot, lenient, latestVersions, allowPreReleases, dependencySubstitution)
    }

    @Override
    def downloadDependency(String dep) {
        log.info("Pulling in $dep")
        def (String name, String version, String classifier) = dep.split(":")

        def projectDetails = cache.getDetails(name)
        version = projectDetails.maybeFixVersion(version)
        def wheelDetails = projectDetails.findVersion(version).find { it.filename == "${name}-${version}-${classifier}.whl" }

        if (wheelDetails == null) {
            if (lenient) {
                log.error("Unable to find wheels for $dep")
                return
            }
            throw new RuntimeException("Unable to find wheels for $dep")
        }

        def destDir = Paths.get(ivyRepoRoot.absolutePath, BINARY_DIST_ORG, name, version, classifier).toFile()
        destDir.mkdirs()

        def wheelArtifact = downloadArtifact(destDir, wheelDetails.url)
        def packageDependencies = new WheelsPackage(wheelArtifact, cache, dependencySubstitution,
            latestVersions, allowPreReleases).dependencies

        new IvyFileWriter(name, version, BINARY_DIST_PACKAGE_TYPE, [wheelDetails]).writeIvyFile(destDir, packageDependencies)

        packageDependencies.each { key, value ->
            dependencies.addAll(value)
        }
    }
}
