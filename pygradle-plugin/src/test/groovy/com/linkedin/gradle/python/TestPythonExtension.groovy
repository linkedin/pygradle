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
package com.linkedin.gradle.python

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.IgnoreIf
import spock.lang.Specification

class TestPythonExtension extends Specification {

    def project = new ProjectBuilder().build()

    // Have to ignore this if its a windows machine trying due to the fact its
    // based on shell
    @IgnoreIf({ Os.isFamily(Os.FAMILY_WINDOWS) })
    def "pythonEnvironment path"() {
        when: "path parts are separated"
        def settings = new PythonExtension(project)
        List<String> parts = settings.pythonEnvironment.get('PATH').toString().tokenize(':')
        then: "they have venv python PATH + system env PATH"
        !parts.empty
        parts.size() > 1
        parts[0].endsWith('/build/venv/bin')
        parts.contains('/bin')
        parts.contains('/usr/bin')
    }

    def 'can add new values to forced versions'() {
        def settings = new PythonExtension(project)

        when:
        settings.forceVersion('a', 'b', 'c')

        then:
        settings.forcedVersions['b'] == ['group': 'a', 'name': 'b', 'version': 'c']

        when:
        settings.forceVersion('a:b:f')

        then:
        settings.forcedVersions['b'] == ['group': 'a', 'name': 'b', 'version': 'f']
    }

    def 'will throw if any value is null'() {
        def settings = new PythonExtension(project)

        when:
        settings.forceVersion(null, 'b', 'c')

        then:
        def ex = thrown(NullPointerException)
        ex.message == 'Group cannot be null'

        when:
        settings.forceVersion('a', null, 'c')

        then:
        ex = thrown(NullPointerException)
        ex.message == 'Name cannot be null'

        when:
        settings.forceVersion('a', 'b', null)

        then:
        ex = thrown(NullPointerException)
        ex.message == 'Version cannot be null'

        when:
        settings.forceVersion(null)

        then:
        ex = thrown(NullPointerException)
        ex.message == 'GAV cannot be null'
    }

    def 'will work with closure to configure python details'() {
        def settings = new PythonExtension(project)

        when:
        settings.details {
            virtualEnvPrompt = 'hello!'
        }

        then:
        noExceptionThrown()
    }

}
