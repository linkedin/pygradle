package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal

class PythonWheelPluginTest extends AbstractBaseRuleSourcePluginTest {

  def 'apply multiple platforms'() {
    when:
    dsl {
      pluginManager.apply PythonWheelPlugin
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
    binaries.withType(WheelBinarySpec).size() == 2
    for(WheelBinarySpec spec : binaries.withType(WheelBinarySpec)) {
      assert spec != null
      assert (spec.tasks.toList().collect { it.name } as Set).containsAll("createPythonWheel${spec.getPythonEnvironment().getVersion().getVersionString()}" as String)
    }

    and:
    binaries.size() == 2
  }
}
