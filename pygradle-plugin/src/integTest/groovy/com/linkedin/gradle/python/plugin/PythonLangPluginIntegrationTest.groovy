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
import nebula.test.IntegrationSpec
import org.junit.Ignore

class PythonLangPluginIntegrationTest extends IntegrationSpec {

    @Ignore
    def 'test things'() {
        setup:
        buildFile << """
        buildscript {
            dependencies {
                classpath files(${System.getenv('test.dependencies').split(',').collect{ "'$it'" }.join(',')})
            }
        }
        """.stripIndent()
        buildFile << "apply plugin: 'python' \n"
        buildFile << """
        model {
            components {
            }
        }
        """

        file('src/main/python/example.py')

        when:
        def executionResult = runTasks('model', 'properties', 'components')

        then:
        executionResult.standardOutput == ''
    }
}
