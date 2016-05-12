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
