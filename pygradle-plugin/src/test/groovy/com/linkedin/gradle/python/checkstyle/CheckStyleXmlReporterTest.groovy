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
import spock.lang.Specification

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class CheckStyleXmlReporterTest extends Specification {

  def 'parses strings properly'() {
    setup:
    def flake8Results = '''coolproject/mod.py:97:1: F401 'shutil' imported but unused
coolproject/mod.py:625:17: E225 missing whitespace around operato
coolproject/mod.py:729:1: F811 redefinition of function 'readlines' from line 723
coolproject/mod.py:1028:1: F841 local variable 'errors' is assigned to but never used
coolproject/mod.py:97:1: F401 'shutil' imported but unused
coolproject/mod.py:625:17: E225 missing whitespace around operator
coolproject/mod.py:729:1: F811 redefinition of unused 'readlines' from line 723
coolproject/mod.py:939:1: C901 'Checker.check_all' is too complex (12)
coolproject/mod.py:1028:1: F841 local variable 'errors' is assigned to but never used
coolproject/mod.py:1204:1: C901 'selftest' is too complex (14)'''

    def container = new FileStyleViolationsContainer()
    flake8Results.eachLine { line -> container.parseLine(line) }

    when:
    def generatedXml = new CheckStyleXmlReporter(container).generateXml()
    def xml = new XmlSlurper(false, false).parseText(generatedXml)

    then:
    noExceptionThrown()
    container.violationMap.size() == 1
    container.violationMap['coolproject/mod.py'].violations.size() == 10

    xml.file.size() == 1
    xml.file[0].error.size() == 10

    when:
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    def schema = factory.newSchema(new StreamSource(this.getClass().getResourceAsStream("/checkstyle/checkstyle.xsd")))
    def validator = schema.newValidator()
    validator.validate(new StreamSource(new StringReader(generatedXml)))

    then:
    noExceptionThrown()
  }
}
