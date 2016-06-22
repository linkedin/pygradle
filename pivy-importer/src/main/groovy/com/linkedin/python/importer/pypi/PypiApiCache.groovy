package com.linkedin.python.importer.pypi

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.http.client.fluent.Request

@Slf4j
class PypiApiCache {

    Map<String, ProjectDetails> cache = [:].withDefault { String name -> new ProjectDetails(downloadMetadata(name)) }

    ProjectDetails getDetails(String project) {
        return cache.get(project)
    }

    private Object downloadMetadata(String dependency) {
        def url = "https://pypi.python.org/pypi/$dependency/json"
        log.debug("Metadata url: {}", url)
        def content = Request.Get(url)
                .connectTimeout(10000)
                .socketTimeout(10000)
                .execute().returnContent().asString();

        def object = new JsonSlurper().parseText(content)
        return object
    }
}
