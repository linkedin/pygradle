package com.linkedin.gradle.python.internal


import com.linkedin.gradle.python.PythonSourceSet
import org.gradle.api.internal.file.FileResolver
import org.gradle.language.base.sources.BaseLanguageSourceSet
import spock.lang.Specification

class DefaultPythonSourceSetTest extends Specification {
  def "has useful string representation"() {
    setup:
    def parent = "main"
    def fileResolver = Mock(FileResolver)
    def sourceSet = BaseLanguageSourceSet.create(PythonSourceSet, DefaultPythonSourceSet, "python", parent, fileResolver)

    expect:
    sourceSet.displayName == "Python source 'main:python'"
    sourceSet.toString() == "Python source 'main:python'"
  }
}
