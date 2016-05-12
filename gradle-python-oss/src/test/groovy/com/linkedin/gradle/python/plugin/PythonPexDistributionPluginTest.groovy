package com.linkedin.gradle.python.plugin


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PythonPexDistributionPluginTest extends Specification {

  def 'can apply python pex plugin resource'() {
    when:
    def project = new ProjectBuilder().build()
    then:
    project.plugins.apply('python-pex')
  }
}
