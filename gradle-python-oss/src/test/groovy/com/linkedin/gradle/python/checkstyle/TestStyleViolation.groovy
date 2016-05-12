package com.linkedin.gradle.python.checkstyle


import com.linkedin.gradle.python.checkstyle.model.StyleViolation
import spock.lang.Specification
import spock.lang.Unroll

class TestStyleViolation extends Specification {

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
