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
package com.linkedin.gradle.python.checkstyle.model


import spock.lang.Specification

class TestFileStyleViolationsContainer extends Specification {

  FileStyleViolationsContainer container = new FileStyleViolationsContainer()

  def 'can parse simple flake8 line'() {
    when:
    container.parseLine('foo/__init__.py:1:1: E265 block comment should start with \'# \'')

    then:
    container.violationMap.size() == 1
    container.violationMap.containsKey('foo/__init__.py')
    container.violationMap['foo/__init__.py'].filename == 'foo/__init__.py'
    container.violationMap['foo/__init__.py'].violations.size() == 1
  }

  def 'when adding lines, can add multiple errors per file'() {
    when:
    container.parseLine('foo/__init__.py:1:1: E265 block comment should start with \'# \'')
    container.parseLine('foo/__init__.py:2:2: E265 block comment should start with \'# \'')

    then:
    container.violationMap.size() == 1
    container.violationMap.containsKey('foo/__init__.py')
    container.violationMap['foo/__init__.py'].filename == 'foo/__init__.py'
    container.violationMap['foo/__init__.py'].violations.size() == 2
    def first = container.violationMap['foo/__init__.py'].violations.first()
    first.violationType == StyleViolation.ViolationType.ERROR
    first.lineNumber == 1
    first.columnNumber == 1

    def last = container.violationMap['foo/__init__.py'].violations.last()
    last.violationType == StyleViolation.ViolationType.ERROR
    last.lineNumber == 2
    last.columnNumber == 2
  }

  def 'added multiple files results in multiple entries'() {
    when:
    container.parseLine('foo/foo.py:1:1: E265 block comment should start with \'# \'')
    container.parseLine('foo/bar.py:2:2: E265 block comment should start with \'# \'')

    then:
    container.violationMap.size() == 2
    container.violationMap.containsKey('foo/foo.py')
    container.violationMap.containsKey('foo/bar.py')
  }
}
