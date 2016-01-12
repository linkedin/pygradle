package com.linkedin.gradle.python.tasks


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

    task.pythonEnvironment = new PythonTestEnvironmentBuilder().build(action, 0)

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
