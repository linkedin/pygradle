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
package com.linkedin.python.importer.pypi

import com.linkedin.python.importer.util.ProxyDetector
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException
import org.apache.http.client.fluent.Request


@Slf4j
class PypiApiCache {

    Map<String, ProjectDetails> cache = [:].withDefault { String name -> new ProjectDetails(downloadMetadata(name)) }

    ProjectDetails getDetails(String project, boolean lenient) {
        try {
            return cache.get(project)
        } catch(HttpResponseException httpResponseException) {
            String msg = "Package ${project} has an illegal module name, " +
                "we are not able to find it on PyPI (https://pypi.org/pypi/$project/json)"
            if (lenient) {
                log.error("$msg. ${httpResponseException.message}")
                return null
            }
            throw new IllegalArgumentException("$msg. ${httpResponseException.message}")
        }
    }

    static private Map<String, Object> downloadMetadata(String dependency) {
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
