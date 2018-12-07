/**
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
package com.linkedin.python.importer

import com.linkedin.python.importer.util.ProxyDetector
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.apache.http.client.fluent.Request

@Slf4j
class PypiClient {

    File downloadArtifact(File destDir, String url) {

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

    Map<String, Object> downloadMetadata(String dependency) {
        def url = "https://pypi.org/pypi/$dependency/json"
        log.debug("Metadata url: {}", url)
        def proxy = ProxyDetector.maybeGetHttpProxy()

        def builder = Request.Get(url)
        if (null != proxy) {
            builder = builder.viaProxy(proxy)
        }
        def content = builder.connectTimeout(10000)
            .socketTimeout(10000)
            .execute().returnContent().asString()

        def object = new JsonSlurper().parseText(content)
        return (Map<String, Object>) object
    }
}
