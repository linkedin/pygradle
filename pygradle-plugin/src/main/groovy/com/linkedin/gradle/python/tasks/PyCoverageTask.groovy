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


import com.linkedin.gradle.python.coverage.CoverageXmlReporter
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.ExecResult

import java.util.regex.Pattern


/**
 * Run coverage and generate outputs. Uses {@link PyTestTask} for filtering inputs
 */
@CompileStatic
class PyCoverageTask extends PyTestTask {

    @OutputDirectory
    File getCoverageOutputDir() {
        return project.file("${ project.buildDir }/coverage")
    }

    private File getCoverageReport() {
        return project.file("${ project.buildDir }/coverage/coverage.xml")
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

    PyCoverageTask() {
        super()
        stdOut = new TeeOutputStream(stdOut, outputStream)
    }

    /**
     * Include coverage data
     */
    public void preExecution() {
        // using subArgs as these must be added after py.test
        subArgs(
            '--cov',
            project.file(component.srcDir).getAbsolutePath(),
            '--cov-report=xml',
            '--cov-report=html',
            '--cov-report=term'
        )

        super.preExecution()
    }

    @Override
    void processResults(ExecResult execResult) {
        ParseOutputStream streamProcessor = new ParseOutputStream()

        streamProcessor.processStream(outputStream.toString())

        String htmlDir = streamProcessor.htmlDir
        String coverage = streamProcessor.coverage

        // If there is no coverage to report, then the htmlDir value will be empty
        if (htmlDir != null) {
            FileUtils.copyDirectoryToDirectory(project.file(htmlDir), coverageOutputDir)
        }

        CoverageXmlReporter coverageXmlReport = new CoverageXmlReporter(coverage)
        coverageReport.text = coverageXmlReport.generateXML()
        super.processResults(execResult)
    }

    static class ParseOutputStream {

        static final Pattern HTML_PATTERN = Pattern.compile('^Coverage HTML written to dir (.*)$')
        static final Pattern XML_PATTERN = Pattern.compile('^Coverage XML written to file (.*)$')
        static final Pattern COVERAGE_PATTERN = Pattern.compile('^TOTAL ((\\s+\\d+)+)%$')

        String coverageXml
        String htmlDir
        String coverage

        void processStream(String stream) {
            List<String> textOutput = stream.readLines()
            for (int i = textOutput.size() - 1; i >= 0; --i) {
                def xmlMatcher = XML_PATTERN.matcher(textOutput.get(i))
                def htmlMatcher = HTML_PATTERN.matcher(textOutput.get(i))
                def coverageMatcher = COVERAGE_PATTERN.matcher(textOutput.get(i))

                if (xmlMatcher.matches()) {
                    coverageXml = xmlMatcher.group(1)
                } else if (htmlMatcher.matches()) {
                    htmlDir = htmlMatcher.group(1)
                } else if (coverageMatcher.matches()) {
                    coverage = coverageMatcher.group(1)
                }

                if (coverageXml != null && htmlDir != null && coverage != null) {
                    break
                }
            }
        }
    }
}
