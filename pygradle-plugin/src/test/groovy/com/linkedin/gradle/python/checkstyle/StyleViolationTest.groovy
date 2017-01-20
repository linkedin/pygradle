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
package com.linkedin.gradle.python.checkstyle


import com.linkedin.gradle.python.checkstyle.model.StyleViolation
import spock.lang.Specification
import spock.lang.Unroll

class StyleViolationTest extends Specification {

  @Unroll
  def 'can parse #code code correctly'() {
    expect:
    new StyleViolation(null, null, code, null).violationType == type

    where:
    code   | type
    'E101' | StyleViolation.ViolationType.ERROR
    'W102' | StyleViolation.ViolationType.WARNING
    'F103' | StyleViolation.ViolationType.PY_FLAKES
    'C104' | StyleViolation.ViolationType.COMPLEXITY
    'N103' | StyleViolation.ViolationType.NAMING
    'Q000' | StyleViolation.ViolationType.OTHER
  }
}
