package com.linkedin.gradle.python.plugin.internal.language


import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin
import com.linkedin.gradle.python.plugin.internal.base.PythonLanguageRulePlugin
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal

class PythonLangRulePluginTest extends AbstractBaseRuleSourcePluginTest {

  def 'apply multiple platforms'() {
    when:
    dsl {
      pluginManager.apply PythonLanguageRulePlugin
      pluginManager.apply PythonBaseRulePlugin
      pluginManager.apply PythonLangRulePlugin
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
        assert (spec.tasks.toList().collect { it.name } as Set).containsAll("createPythonWheel${spec.getPythonEnvironment().getVersion().getVersionString().replace('.', '')}" as String)
    }

    and:
    binaries.withType(SourceDistBinarySpec).size() == 1
    for(SourceDistBinarySpec spec : binaries.withType(SourceDistBinarySpec)) {
      assert spec != null
      assert (spec.tasks.toList().collect { it.name } as Set).containsAll('createPythonSourceDist')
    }

    and:
    binaries.size() == 3
  }
}
