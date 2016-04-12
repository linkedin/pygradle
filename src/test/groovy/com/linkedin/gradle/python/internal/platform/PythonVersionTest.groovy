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

package com.linkedin.gradle.python.internal.platform


import spock.lang.Specification
import spock.lang.Unroll

class PythonVersionTest extends Specification {

  def 'when null comes in, will be throw'() {
    when:
    PythonVersion.parse(null)

    then:
    def e = thrown(IllegalArgumentException)
    e.message == 'PythonVersion cannot be null!'
  }

  @Unroll
  def 'when python#version comes it, it will be the version'() {
    expect:
    PythonVersion.parse("python$version") == new PythonVersion(version)

    where:
    version << ['2.3.4', '2.3', '2', '3']
  }

  @Unroll
  def 'when #version comes it, it will be the version'() {
    expect:
    PythonVersion.parse(version) == new PythonVersion(version)

    where:
    version << ['2.3.4', '2.3', '2', '3', '2.7.10']
  }

  @Unroll
  def 'when #version comes in, will throw'() {
    when:
    PythonVersion.parse(version)

    then:
    def e = thrown(IllegalArgumentException)
    e.message == "Unable to accept `$version` as a PythonVersion".toString()

    where:
    version << ['abc', 'python1234567']
  }

  @Unroll
  def 'can understand #full will be #majorMinor'() {
    expect:
    PythonVersion.parse(full).getMajorMinorVersion() == majorMinor

    where:
    full      | majorMinor
    '2.4.7'   | '2.4'
    '2.4.10'  | '2.4'
    '3.15.10' | '3.15'
  }
}
