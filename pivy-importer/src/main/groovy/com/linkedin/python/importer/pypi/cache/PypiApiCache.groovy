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
package com.linkedin.python.importer.pypi.cache

import com.linkedin.python.importer.PypiClient
import com.linkedin.python.importer.pypi.ProjectDetails
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpResponseException

@Slf4j
class PypiApiCache implements ApiCache {
    PypiClient pypiClient = new PypiClient()
    Map<String, ProjectDetails> cache = [:].withDefault { String name ->
        new ProjectDetails(pypiClient.downloadMetadata(name))
    }

    ProjectDetails getDetails(String project) {
        try {
            return cache.get(project)
        } catch (HttpResponseException httpResponseException) {
            String msg = "Package ${project} has an illegal module name, " +
                "we are not able to find it on PyPI (https://pypi.org/pypi/$project/json)"
            throw new IllegalArgumentException("$msg. ${httpResponseException.message}")
        }
    }
}
