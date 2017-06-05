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
import com.linkedin.python.importer.pypi.PypiApiCache
import com.linkedin.python.importer.util.ProxyDetector
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.fluent.Request

import java.nio.file.Paths

@Slf4j
class DependencyDownloader {


    Queue<String> dependencies = [] as Queue
    Set<String> processedDependencies = [] as Set
    PypiApiCache cache = new PypiApiCache()
    File ivyRepoRoot
    DependencySubstitution dependencySubstitution
    boolean latestVersions
    boolean allowPreReleases

    DependencyDownloader(String project, File ivyRepoRoot, DependencySubstitution dependencySubstitution,
                         boolean latestVersions, boolean allowPreReleases) {
        this.dependencySubstitution = dependencySubstitution
        this.ivyRepoRoot = ivyRepoRoot
        this.latestVersions = latestVersions
        this.allowPreReleases = allowPreReleases
        dependencies.add(project)
    }

    def download() {
        while (!dependencies.isEmpty()) {
            def dep = dependencies.poll()
            if (dep in processedDependencies) {
                continue
            }
            downloadDependency(dep)
            processedDependencies.add(dep)
        }
    }

    def downloadDependency(String dep) {
        log.info("Pulling in $dep")
        def (name, version) = dep.split(":")

        def projectDetails = cache.getDetails(name)
        version = projectDetails.maybeFixVersion(version)
        def sdistDetails = projectDetails.findVersion(version).find { it.packageType == 'sdist' }

        if (sdistDetails == null) {
            throw new RuntimeException("Unable to find source dist for $dep")
        }

        def destDir = Paths.get(ivyRepoRoot.absolutePath, "pypi", name, version).toFile()

        destDir.mkdirs()

        def artifact = downloadArtifact(destDir, sdistDetails.url)
        def packageDependencies = new SourceDistPackage(artifact, cache, dependencySubstitution,
                                                        latestVersions, allowPreReleases).dependencies

        new IvyFileWriter(name, version, [sdistDetails], packageDependencies).writeIvyFile(destDir)

        packageDependencies.each { key, value ->
            dependencies.addAll(value)
        }
    }

    static File downloadArtifact(File destDir, String url) {

        def filename = FilenameUtils.getName(new URL(url).getPath())
        def contents = new File(destDir, filename)

        if (!contents.exists()) {
            def proxy = ProxyDetector.maybeGetHttpProxy()

            def builder = Request.Get(url)
            if (null != proxy) {
                builder = builder.viaProxy(proxy)
            }

            builder.connectTimeout(5000)
                .socketTimeout(5000)
                .execute().saveContent(contents)
        }

        return contents
    }
}
