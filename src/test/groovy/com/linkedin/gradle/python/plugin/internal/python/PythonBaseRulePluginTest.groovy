package com.linkedin.gradle.python.plugin.internal.python


import com.linkedin.gradle.python.PythonSourceSet
import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.base.PythonLanguageRulePlugin
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal
import org.gradle.model.ModelMap

class PythonBaseRulePluginTest extends AbstractBaseRuleSourcePluginTest {
  def "creates python source set with conventional locations for components"() {
    when:
    dsl {
      pluginManager.apply PythonLanguageRulePlugin
      pluginManager.apply PythonBaseRulePlugin
      model {
        components {
          python(com.linkedin.gradle.python.spec.component.PythonComponentSpec) {
            targetPlatform 'python2.7'
            binaries {
              wheel(com.linkedin.gradle.python.spec.binary.WheelBinarySpec) {
                targets '2.7'
              }
              source(com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec) {
                targets 'python2.7'
              }
            }
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
      pluginManager.apply PythonLanguageRulePlugin
      pluginManager.apply PythonBaseRulePlugin
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
    binaries.size() == 0
  }
}
