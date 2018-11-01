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

import com.linkedin.python.importer.ImporterCLI
import com.linkedin.python.importer.distribution.WheelsPackage
import com.linkedin.python.importer.ivy.IvyFileWriter

import groovy.util.logging.Slf4j
import java.nio.file.Paths

@Slf4j
class WheelsDownloader extends DependencyDownloader {
    static final String BINARY_DIST_PACKAGE_TYPE = "bdist_wheel"
    static final String BINARY_DIST_ORG = "wheel"

    WheelsDownloader(
        String project,
        File ivyRepoRoot,
        DependencySubstitution dependencySubstitution,
        Set<String> processedDependencies) {

        super(project, ivyRepoRoot, dependencySubstitution, processedDependencies)
    }

    /**
     * The module names in Wheel artifact names are using "_" to replace "-", eg., python-submit,
     * its wheel artifact is python_subunit-1.3.0-py2.py3-none-any.whl.
     * @param name
     * @return
     */
    static String translateNameToWheelFormat(String name) {
        return name.replaceAll("-", "_")
    }

    @Override
    def downloadDependency(
            String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient) {

        def (String name, String version, String classifier) = dep.split(":")

        name = translateNameToWheelFormat(name)
        def projectDetails = cache.getDetails(name, lenient)
        // project name is illegal, which means we can't find any information about this project on PyPI
        if (projectDetails == null) {
            return
        }

        version = projectDetails.maybeFixVersion(version)
        def wheelDetails = projectDetails
                            .findVersion(version)
                            .find { it.filename.equalsIgnoreCase("${name}-${version}-${classifier}.whl") }

        if (wheelDetails == null) {
            if (lenient) {
                log.error("Unable to find wheels for $dep")
                return
            }
            throw new RuntimeException("Unable to find wheels for $dep")
        }

        // make sure the module name has the same letter case as PyPI
        name = getActualModuleNameFromFilename(wheelDetails.filename, version)
        log.info("Pulling in $name:$version:$classifier")

        def destDir = Paths.get(ivyRepoRoot.absolutePath, BINARY_DIST_ORG, name, version, classifier).toFile()
        destDir.mkdirs()

        def wheelArtifact = downloadArtifact(destDir, wheelDetails.url)
        def packageDependencies = new WheelsPackage(name, version, wheelArtifact, cache, dependencySubstitution)
            .getDependencies(latestVersions, allowPreReleases, fetchExtras, lenient)

        log.debug("The dependencies of package $project: is ${packageDependencies.toString()}")
        new IvyFileWriter(name, version, BINARY_DIST_PACKAGE_TYPE, [wheelDetails])
            .writeIvyFile(destDir, packageDependencies, classifier)

        packageDependencies.each { key, value ->
            List<String> sdistDependencies = value
            for (String sdist : sdistDependencies) {
                DependencyDownloader sdistDownloader = new SdistDownloader(
                    sdist, ivyRepoRoot, dependencySubstitution, processedDependencies)

                ImporterCLI.pullDownPackageAndDependencies(
                    processedDependencies, sdistDownloader, latestVersions, allowPreReleases, fetchExtras, lenient)
            }
        }
    }
}
