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


import com.linkedin.gradle.python.checkstyle.model.FileStyleViolationsContainer
import groovy.xml.MarkupBuilder

class CheckStyleXmlReporter {

  private final FileStyleViolationsContainer violationContainer
  CheckStyleXmlReporter(FileStyleViolationsContainer container) {
    this.violationContainer = container;
  }

  public String generateXml() {
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    xml.checkstyle() {
      violationContainer.getViolations().each { fileStyleViolation ->
        file(name: fileStyleViolation.filename) {
          fileStyleViolation.violations.each { violation ->
            error(violation.createChecktyleMap())
          }
        }
      }
    }

    writer.toString()
  }
}
