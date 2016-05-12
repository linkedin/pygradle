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

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  @OutputFile
  File checkstyleReport = project.file("${project.buildDir}/checkstyle.xml")

  CheckStyleGeneratorTask() {
    stdOut = outputStream;
    errOut = outputStream;
    ignoreExitValue = true;
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
