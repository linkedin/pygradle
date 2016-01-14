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
    1 * action.args(["--formats=gztar,zip"])
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
