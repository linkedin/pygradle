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
package com.linkedin.gradle.python.plugin

class PyGradleTestBuilder {

    static String createSetupCfg() {
        return '''\
            | [flake8]
            | ignore = E121,E123,E226,W292
            | max-line-length = 160
            |
            | [pytest]
            | addopts = --ignore build/ --ignore dist/
            '''.stripMargin().stripIndent()
    }

    static String createSetupPy() {
        return PyGradleTestBuilder.getResource("/templates/setup.py.template").text + '\n' + PyGradleTestBuilder.getResource("/setup.py").text
    }

    static String createSettingGradle() {
        return """\
            | rootProject.name = 'testProject'
            |""".stripMargin().stripIndent()
    }

    static String createRepoClosure() {
        return """\
            |repositories {
            |  ivy {
            |    name 'pypi-local'
            |    url "${System.getenv('TEST_REPO')}"
            |    layout "pattern", {
            |      ivy "[organisation]/[module]/[revision]/[module]-[revision].ivy"
            |      artifact "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
            |      m2compatible = true
            |    }
            |  }
            |}""".stripMargin().stripIndent()
    }
}
