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

package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.plugin.internal.AbstractBaseRuleSourcePluginTest
// DO NOT IMPORT ANY OF THE SPEC!!!!!!!!!!!
// This test is to ensure that fully qualified class are not required.

class PythonBaseLangPluginTest extends AbstractBaseRuleSourcePluginTest {

  def 'dsl does not need full classes'() {
    when:
    dsl {
      pluginManager.apply PythonBaseLangPlugin
      model {
        components {
          python(PythonComponentSpec) {
            targetPlatform 'python2.7'
            binaries {
              wheel(WheelBinarySpec) {
                targets '2.7'
              }
              source(SourceDistBinarySpec) {
                targets 'python2.7'
              }
            }
            sources {
              pyTest(PythonTestSourceSet) {}
              pyMain(PythonSourceSet) {}
            }
          }
        }
      }
    }

    then:
    realizeComponents()
    noExceptionThrown()
  }

  /**
   * If this test fails then you have included one of the classes below is in your imports.
   * This breaks the validity of {@link #'dsl does not need full classes'()}. (above test)
   *
   * We are testing the test here.
   */
  def 'make sure that fully qualified class are not included'() {
    when:
    PythonComponentSpec.class != null

    then:
    thrown(MissingPropertyException)

    when:
    WheelBinarySpec.class != null

    then:
    thrown(MissingPropertyException)

    when:
    SourceDistBinarySpec.class != null

    then:
    thrown(MissingPropertyException)

    when:
    PythonTestSourceSet.class != null

    then:
    thrown(MissingPropertyException)

    when:
    PythonSourceSet.class != null

    then:
    thrown(MissingPropertyException)
  }
}
