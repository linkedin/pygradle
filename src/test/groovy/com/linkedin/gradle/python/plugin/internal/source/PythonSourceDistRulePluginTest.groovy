package com.linkedin.gradle.python.plugin.internal.source


import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import com.linkedin.gradle.python.plugin.internal.DefaultPythonTaskRule
import com.linkedin.gradle.python.plugin.internal.wheel.PythonWheelRulePlugin
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
            targetPlatform 'python2.7'
          }
        }
      }
    }


    then:
    def components = realizeComponents()
    def source = components.source
    source.targetPlatforms == [DefaultPlatformRequirement.create("python2.7")]
    source.sources instanceof ModelMap
    source.sources.python instanceof PythonSourceSet
    source.sources.python.source.srcDirs == [project.getPythonPath("src/main/python")] as Set

    and:
    def sources = realizeSourceSets()
    sources as Set == source.sources as Set
  }

  def 'apply multiple platforms'() {
    when:
    dsl {
      pluginManager.apply BasePythonRulePlugin
      pluginManager.apply DefaultPythonTaskRule
      pluginManager.apply PythonSourceDistRulePlugin
      model {
        components {
          source(com.linkedin.gradle.python.spec.component.SourceDistComponentSpec) {
            targetPlatform 'python2.7'
            targetPlatform 'python2.6'
            targetPlatform 'python3.1'
          }
        }
      }
    }

    then:
    def components = realizeComponents()
    def binaries = realizeBinaries()
    def source = components.source
    ['2.7', '2.6', '3.1'].each { version ->
      assert source.targetPlatforms.contains(DefaultPlatformRequirement.create("python$version"))
      def binary = binaries.get("source")
      assert binary != null
      assert (binary.tasks.toList().collect { it.name } as Set).containsAll(defaultTasks(version))
    }
    binaries.size() == 1
  }
}
