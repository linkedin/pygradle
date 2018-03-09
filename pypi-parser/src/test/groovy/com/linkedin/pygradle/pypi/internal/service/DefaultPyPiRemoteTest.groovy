package com.linkedin.pygradle.pypi.internal.service

import groovy.json.JsonSlurper
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

class DefaultPyPiRemoteTest extends Specification {

    def 'can parse pytest response'() {
        def server = new MockWebServer()
        server.enqueue(makeResponse('pytest'))
        server.start()

        def url = server.url("/pypi/")
        def remote = new DefaultPyPiRemote(url.toString(), new OkHttpClient.Builder().build())

        when:
        def packageDetails = remote.resolvePackage('pytest')

        then:
        noExceptionThrown()
        packageDetails.version.collect { it.toVersionString() } == getVersionsFor('pytest')
        packageDetails.latestVersion.toVersionString() == '3.1.0'
        packageDetails.packageName == 'pytest'

        and:
        expect:
        server.takeRequest().path == '/pypi/pytest/json'
        server.shutdown()
    }

    List<String> getVersionsFor(String packageName) {
        def json = new JsonSlurper().parse(DefaultPyPiRemoteTest.classLoader
            .getResourceAsStream("sample/pypi/${ packageName }.json"))

        def returnValue = []
        returnValue.addAll(json['releases'].keySet())
        return returnValue.sort()
    }

    MockResponse makeResponse(String packageName) {
        def text = DefaultPyPiRemoteTest.classLoader.getResourceAsStream("sample/pypi/${ packageName }.json").text
        return new MockResponse().setBody(text)
    }
}
