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
package com.linkedin.gradle.python.coverage

import groovy.xml.MarkupBuilder

class CoverageXmlReporter {
    private final String coverageInfo

    CoverageXmlReporter(String coverageInfo) {
        this.coverageInfo = coverageInfo
    }

    public String generateXML() {
        def xmlWriter = new StringWriter()
        def xmlMarkup = new MarkupBuilder(xmlWriter)
        def coverage = getCoverage()
        def missedStatements = coverage['missed_statements']
        def coveredStatements = coverage['total_statements'] - coverage['missed_statements']

        xmlMarkup.report(name: "coverage") {
            "package"(name: "coverage") {
                "class"(name: "coverage/coverage") {
                    counter(type: "LINE", missed: missedStatements, covered: coveredStatements)
                }
            }
        }
        return xmlWriter.toString()
    }

    private Map<String, Integer> getCoverage() {
        def group = (coverageInfo =~ /\d+/)
        def missedStatements = 0
        def totalStatements = 0

        if (group.size() >= 3) {
            totalStatements = group[0].toInteger()
            missedStatements = group[1].toInteger()
        }

        return [total_statements: totalStatements, missed_statements: missedStatements]
    }
}

