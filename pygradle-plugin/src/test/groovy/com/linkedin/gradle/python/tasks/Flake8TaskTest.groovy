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
package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.plugin.PythonPlugin
import com.linkedin.gradle.python.util.ExtensionUtils
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class Flake8TaskTest extends Specification {

    def 'python details will use existing details from project'() {
        def project = new ProjectBuilder().build()
        project.plugins.apply(PythonPlugin)

        def flake8Task = project.tasks.flake8 as Flake8Task
        def extension = ExtensionUtils.getPythonExtension(project)
        def firstInterpreter = extension.details.systemPythonInterpreter

        when:
        true

        then:
        flake8Task.getPythonDetails().getSystemPythonInterpreter() == extension.details.systemPythonInterpreter

        when:
        extension.details.setPythonInterpreter(extension.details.getPythonVersion(), new File("/foo/bar"))

        then:
        flake8Task.getPythonDetails().getSystemPythonInterpreter() != firstInterpreter
        flake8Task.getPythonDetails().getSystemPythonInterpreter() == extension.details.systemPythonInterpreter

    }
}
