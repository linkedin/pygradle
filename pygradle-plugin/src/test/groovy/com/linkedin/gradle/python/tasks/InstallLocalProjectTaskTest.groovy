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


import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentTestDouble
import org.gradle.api.Project
import org.gradle.process.internal.ExecAction
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class InstallLocalProjectTaskTest extends Specification {

  @Rule
  TemporaryFolder temporaryFolder
  Project project
  InstallLocalProjectTask task

  def setup() {
    project = ProjectBuilder.builder().build()
    task = project.tasks.create('test', InstallLocalProjectTask)
  }

  def 'can install local project'() {
    given:
    def action = Mock(ExecAction)

    task.pythonEnvironment = new PythonEnvironmentTestDouble(action, 0)

    when:
    task.installLocalProject()

    then:
    noExceptionThrown()
    1 * action.args(*_) >> { args ->
      List<String> argList = args[0]
      assert argList[0] == task.venvDir.absolutePath + '/bin/pip'
      assert argList[1] == 'install'
      assert argList[2] == '--editable'
      assert argList[3] == project.getProjectDir().getAbsolutePath()
    }
  }
}
