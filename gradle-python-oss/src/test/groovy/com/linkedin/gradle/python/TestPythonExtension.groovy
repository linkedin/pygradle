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

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TestPythonExtension extends Specification {

    def project = new ProjectBuilder().build()

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

}
