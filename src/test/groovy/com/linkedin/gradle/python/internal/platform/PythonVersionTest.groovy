package com.linkedin.gradle.python.internal.platform


import spock.lang.Specification
import spock.lang.Unroll

class PythonVersionTest extends Specification {

  def 'when null comes in, will be throw'() {
    when:
    PythonVersion.parse(null)

    then:
    def e = thrown(IllegalArgumentException)
    e.message == 'PythonVersion cannot be null!'
  }

  def 'when full version comes it, it will be the same'() {
    expect:
    PythonVersion.parse('2.3.4') == new PythonVersion('2.3.4')
    PythonVersion.parse('2.3') == new PythonVersion('2.3')
  }

  @Unroll
  def 'when python#version comes it, it will be the version'() {
    expect:
    PythonVersion.parse("python$version") == new PythonVersion(version)

    where:
    version << ['2.3.4', '2.3']
  }

  @Unroll
  def 'when #version comes in, will throw'() {
    when:
    PythonVersion.parse(version)

    then:
    def e = thrown(IllegalArgumentException)
    e.message == "Unable to accept $version as a PythonVersion".toString()

    where:
    version << ['abc', 'python1234567']
  }
}
