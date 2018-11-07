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

import com.linkedin.python.importer.pypi.PypiApiCache
import com.linkedin.python.importer.util.ProxyDetector
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.fluent.Request

@Slf4j
abstract class DependencyDownloader {
    Queue<String> dependencies = [] as Queue
    PypiApiCache cache = new PypiApiCache()

    String project
    File ivyRepoRoot
    DependencySubstitution dependencySubstitution
    Set<String> processedDependencies

    protected DependencyDownloader(
        String project,
        File ivyRepoRoot,
        DependencySubstitution dependencySubstitution,
        Set<String> processedDependencies) {

        this.project = project
        this.ivyRepoRoot = ivyRepoRoot
        this.dependencySubstitution = dependencySubstitution
        this.processedDependencies = processedDependencies
        dependencies.add(project)
    }

    def download(boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient) {
        while (!dependencies.isEmpty()) {
            def dep = dependencies.poll()
            if (dep in processedDependencies) {
                continue
            }
            downloadDependency(dep, latestVersions, allowPreReleases, fetchExtras, lenient)
            processedDependencies.add(dep)
        }
    }

    abstract downloadDependency(
        String dep, boolean latestVersions, boolean allowPreReleases, boolean fetchExtras, boolean lenient)

    protected static File downloadArtifact(File destDir, String url) {

        def filename = FilenameUtils.getName(new URL(url).getPath())
        def contents = new File(destDir, filename)

        if (!contents.exists()) {
            def proxy = ProxyDetector.maybeGetHttpProxy()

            def builder = Request.Get(url)
            if (null != proxy) {
                builder = builder.viaProxy(proxy)
            }

            for (int i = 0; i < 3; i++) {
                try {
                    builder.connectTimeout(5000)
                        .socketTimeout(5000)
                        .execute()
                        .saveContent(contents)
                    break
                } catch (SocketTimeoutException ignored) {
                    Thread.sleep(1000)
                }
            }
        }

        return contents
    }

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
