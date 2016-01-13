package com.linkedin.gradle.python.plugin.internal.python


import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin
import com.linkedin.gradle.python.plugin.internal.PythonRulePlugin
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal
import org.gradle.model.ModelMap

class PythonRulePluginTest extends AbstractBaseRuleSourcePluginTest {
  def "creates python source set with conventional locations for components"() {
    when:
    dsl {
      pluginManager.apply BasePythonRulePlugin
      pluginManager.apply PythonRulePlugin
      model {
        components {
          python(com.linkedin.gradle.python.spec.component.PythonComponentSpec) {
            targetPlatform 'python2.7'
          }
        }
      }
    }


    then:
    def components = realizeComponents()
    def python = components.python
    python.sources instanceof ModelMap
    python.sources.python instanceof PythonSourceSet
    python.sources.python.source.srcDirs == [project.file("src/main/python")] as Set

    and:
    def sources = realizeSourceSets()
    sources as Set == python.sources as Set

    and:
    def binaries = realizeBinaries()
    binaries.size() == 2
  }


  def 'apply multiple platforms'() {
    when:
    dsl {
      pluginManager.apply BasePythonRulePlugin
      pluginManager.apply PythonRulePlugin
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
    python.getPythonEnvironments().size() == 2
    ['2.7', '2.6'].each { version ->
      def binary = binaries.get("pythonWheel$version")
      assert binary != null
      assert (binary.tasks.toList().collect { it.name } as Set).containsAll("createPythonWheel${version.replace('.', '')}" as String)
    }

    and: //source dist check
    def binary = binaries.get("pythonSourceDist")
    assert binary != null
    assert (binary.tasks.toList().collect { it.name } as Set).containsAll('createPythonSourceDist')

    and:
    binaries.size() == 3 //two wheels, one source dist
  }
}
