/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.gradle.python.plugin.internal.python


import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal
import org.gradle.model.ModelMap

class PythonBaseRulePluginTest extends AbstractBaseRuleSourcePluginTest {
  def "creates python source set with conventional locations for components"() {
    when:
    dsl {
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
    python.sources.size() == 0

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
