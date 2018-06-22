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
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.fluent.Request

@Slf4j
abstract class DependencyDownloader {
    Queue<String> dependencies = [] as Queue
    Set<String> processedDependencies = [] as Set
    PypiApiCache cache = new PypiApiCache()

    File ivyRepoRoot
    boolean lenient
    boolean latestVersions
    boolean allowPreReleases
    DependencySubstitution dependencySubstitution

    protected DependencyDownloader(String project, File ivyRepoRoot, boolean lenient, boolean latestVersions,
                                   boolean allowPreReleases, DependencySubstitution dependencySubstitution) {

        this.ivyRepoRoot = ivyRepoRoot
        this.lenient = lenient
        this.latestVersions = latestVersions
        this.allowPreReleases = allowPreReleases
        this.dependencySubstitution = dependencySubstitution

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

    abstract downloadDependency(String dep)

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
}
