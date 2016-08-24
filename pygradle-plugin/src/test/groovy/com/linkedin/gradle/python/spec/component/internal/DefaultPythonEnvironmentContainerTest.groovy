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

package com.linkedin.gradle.python.spec.component.internal


import com.linkedin.gradle.python.internal.platform.PythonVersion
import org.gradle.process.internal.ExecActionFactory
import spock.lang.Specification
import spock.lang.Unroll

class DefaultPythonEnvironmentContainerTest extends Specification {

  @Unroll
  def 'can parse #input to find PythonEnvironment'() {
    given:
    def container = new DefaultPythonEnvironmentContainer(new File('/build'), 'test', Mock(ExecActionFactory))
    ['2.3.4', '2.6.9', '2.7.9', '3.3.12'].each {
      container.pythonEnvironmentMap.put(PythonVersion.parse(it), new PythonEnvironmentTestDouble(it, null, 0))
    }

    expect:
    container.getPythonEnvironment(input) != null

    where:
    input <<
        ['2.3.4', '2.6.9', '2.7.9', '3.3.12', 'python2.3.4', 'python2.6.9', 'python2.7.9', 'python3.3.12',
         'python2.3', 'python2.6', 'python2.7', 'python3.3', 'python2', 'python3', 'python']
  }
}
