package com.linkedin.gradle.python.plugin.internal.source


import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import com.linkedin.gradle.python.plugin.internal.DefaultPythonTaskRule
import com.linkedin.gradle.python.plugin.internal.wheel.PythonWheelRulePlugin
import com.linkedin.gradle.python.spec.component.internal.DefaultPythonTargetPlatform
import org.gradle.internal.os.OperatingSystem
import org.gradle.model.ModelMap
import org.gradle.platform.base.internal.DefaultPlatformRequirement

class PythonSourceDistRulePluginTest extends AbstractBaseRuleSourcePluginTest {

  def "creates python source set with conventional locations for components"() {
    when:
    dsl {
      pluginManager.apply BasePythonRulePlugin
      pluginManager.apply DefaultPythonTaskRule
      pluginManager.apply PythonSourceDistRulePlugin
      model {
        components {
          source(com.linkedin.gradle.python.spec.component.SourceDistComponentSpec) {
          }
        }
      }
    }


    then:
    def components = realizeComponents()
    def source = components.source
      source.sources instanceof ModelMap
    source.sources.python instanceof PythonSourceSet
    source.sources.python.source.srcDirs == [project.file("src/main/python")] as Set

    and:
    def sources = realizeSourceSets()
    sources as Set == source.sources as Set
  }
}
