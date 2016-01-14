package com.linkedin.gradle.python.spec.component.internal


import com.linkedin.gradle.python.internal.platform.PythonVersion
import org.gradle.process.internal.ExecActionFactory
import spock.lang.Specification
import spock.lang.Unroll

class DefaultPythonEnvironmentContainerTest extends Specification {

  @Unroll
  def 'can parse #input to find PythonEnvironment'() {
    given:
    def container = new DefaultPythonEnvironmentContainer(new File('/build'), 'test', Mock(ExecActionFactory))
    ['2.3.4', '2.6.9', '2.7.9', '3.3.12'].each {
      container.pythonEnvironmentMap.put(PythonVersion.parse(it), new PythonEnvironmentTestDouble(it, null, 0))
    }

    expect:
    container.getPythonEnvironment(input) != null

    where:
    input << ['2.3.4', '2.6.9', '2.7.9', '3.3.12']
  }
}
