package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal

class PythonSourceDistPluginTest extends AbstractBaseRuleSourcePluginTest {

  def 'apply multiple platforms'() {
    when:
    dsl {
      pluginManager.apply PythonSourceDistPlugin
      model {
        components {
          python(com.linkedin.gradle.python.spec.component.PythonComponentSpec) {
            targetPlatform 'python2.7'
            targetPlatform 'python2.6'
          }
        }
      }
    }

    then:
    def components = realizeComponents()
    def binaries = realizeBinaries()
    def python = components.python as PythonComponentSpecInternal

    and:
    python.getPythonEnvironments().size() == 2


    and:
    binaries.withType(SourceDistBinarySpec).size() == 1
    for(SourceDistBinarySpec spec : binaries.withType(SourceDistBinarySpec)) {
      assert spec != null
      assert (spec.tasks.toList().collect { it.name } as Set).containsAll('createPythonSourceDist')
    }

    and:
    binaries.size() == 1
  }
}
