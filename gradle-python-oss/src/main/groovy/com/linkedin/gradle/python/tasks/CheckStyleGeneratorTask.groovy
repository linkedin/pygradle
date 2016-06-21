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
package com.linkedin.gradle.python.tasks


import com.linkedin.gradle.python.checkstyle.CheckStyleXmlReporter
import com.linkedin.gradle.python.checkstyle.model.FileStyleViolationsContainer
import groovy.transform.CompileStatic
import org.gradle.api.tasks.OutputFile
import org.gradle.process.ExecResult


/**
 * This has slightly different behavior from {@link Flake8Task} in that, if the inputs fail violation this will continue.
 *
 * This was done so that a report will be generated vs stopping the gradle process and never generating reports.
 */
@CompileStatic
class CheckStyleGeneratorTask extends Flake8Task {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

  @OutputFile
  File checkstyleReport = project.file("${project.buildDir}/checkstyle.xml")

  CheckStyleGeneratorTask() {
    stdOut = outputStream
    errOut = outputStream
    ignoreExitValue = true
  }

  @Override
  void processResults(ExecResult execResult) {
    def container = new FileStyleViolationsContainer()

    outputStream.toString().readLines().each { String line ->
      container.parseLine(line)
    }

    def reporter = new CheckStyleXmlReporter(container)
    checkstyleReport.text = reporter.generateXml()
  }
}
