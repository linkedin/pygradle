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
import nebula.test.dependencies.DependencyGraphBuilder
import nebula.test.dependencies.GradleDependencyGenerator
import nebula.test.dependencies.ModuleBuilder
import org.gradle.api.Project
import org.gradle.process.internal.ExecAction
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class InstallDependenciesTaskTest extends Specification {
  @Rule
  TemporaryFolder temporaryFolder
  Project project
  InstallDependenciesTask installDependenciesTask

  def setup() {
    project = ProjectBuilder.builder().build()
    installDependenciesTask = project.tasks.create('install', InstallDependenciesTask)
  }

  def 'can execute pip install'() {
    given:
    def graph = new DependencyGraphBuilder()
        .addModule(new ModuleBuilder('test.example:foo:1.0.0')
        .addDependency('g4:a4:4.0.1')
        .addDependency('g5', 'a5', '5.0.1').build()
    ).build()
    def generator = new GradleDependencyGenerator(graph)
    def repo = generator.generateTestMavenRepo()

    project.repositories.maven { url repo.absoluteFile }
    def foo = project.configurations.create('foo')
    project.dependencies.add('foo', 'test.example:foo:1.0.0')

    def action = Mock(ExecAction)

    installDependenciesTask.dependencyConfiguration = foo
    installDependenciesTask.pythonEnvironment = new PythonEnvironmentTestDouble(action, 0)

    when:
    installDependenciesTask.installDependencies()

    then:
    3 * action.args(*_) >> { args ->
      assert args[0][0] == installDependenciesTask.venvDir.absolutePath + '/bin/pip'
      assert args[0][1] == 'install'
      assert args[0][2] == '--no-deps'
      assert args[0][3].startsWith(repo.absoluteFile.absolutePath)
      return null
    }
  }

}
