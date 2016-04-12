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

class BuildSourceDistTaskTest extends Specification {

  @Rule
  TemporaryFolder temporaryFolder
  Project project
  BuildSourceDistTask sourceDistTask
  File distPath

  def setup() {
    project = ProjectBuilder.builder().build()
    sourceDistTask = project.tasks.create('sourceDist', BuildSourceDistTask)
    sourceDistTask.setOutputFormat("gztar")
    new File(sourceDistTask.getTemporaryDir(), 'foo.txt').text = 'some text'
    distPath = temporaryFolder.newFolder()
    sourceDistTask.distributablePath = distPath
  }

  def 'test will call setup.py with the right parameters'() {
    given:
    ExecAction action = Mock(ExecAction)
    sourceDistTask.setPythonEnvironment(new PythonEnvironmentTestDouble(action, 0))

    when:
    sourceDistTask.buildSourceDist()

    then:
    1 * action.args("setup.py", "sdist", "--dist-dir", _)
    1 * action.args(["--formats=gztar"])
  }

  def 'test will call setup.py with the right parameters, on failure will throw exception'() {
    given:
    ExecAction action = Mock(ExecAction)

    sourceDistTask.setPythonEnvironment(new PythonEnvironmentTestDouble(action, 2))

    when:
    sourceDistTask.buildSourceDist()

    then:
    thrown(RuntimeException)
  }
}
