package com.linkedin.gradle.python.coverage

import groovy.xml.MarkupBuilder

class CoverageXmlReporter {
  private final String coverageInfo

  CoverageXmlReporter(String coverageInfo) {
    this.coverageInfo = coverageInfo
  }

  public String generateXML(){
    def xmlWriter = new StringWriter()
    def xmlMarkup = new MarkupBuilder(xmlWriter)
    def coverage = getCoverage()
    def missed_statements = coverage['missed_statements']
    def covered_statements = coverage['total_statements'] - coverage['missed_statements']
    xmlMarkup.report(name: "coverage") {
      "package"(name: "coverage") {
        "class"(name: "coverage/coverage") {
          counter(type: "LINE", missed: missed_statements, covered: covered_statements)
        }
      }
    }
    return xmlWriter.toString()
  }

  private Map<String, Integer> getCoverage() {
    def group = (coverageInfo =~ /\d+/)
    if( group.size() < 3) {
      return [total_statements: 0, missed_statements: 0]
    } else {
      return [total_statements: group[0].toInteger(), missed_statements: group[1].toInteger()]
    }
  }
}

