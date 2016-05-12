package com.linkedin.gradle.python.tasks


import com.linkedin.gradle.python.coverage.CoverageXmlReporter
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.process.ExecResult

import java.util.regex.Pattern


/**
 * Run coverage and generate outputs. Uses {@link PyTestTask} for filtering inputs
 */
@CompileStatic
class PyCoverageTask extends PyTestTask {

    @OutputDirectory
    File coverageOutputDir = project.file("${project.buildDir}/coverage")

    @OutputFile
    File coverageReport = project.file("${project.buildDir}/coverage/coverage.xml")

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

    PyCoverageTask() {
        super()
        stdOut = new TeeOutputStream(stdOut, outputStream)
    }

    /**
     * Include coverage data
     */
    public void preExecution() {
        args('--cov',
            project.file(component.srcDir).getAbsolutePath(),
            '--cov-report=xml',
            '--cov-report=html',
            '--cov-report=term')

        super.preExecution()
    }

    @Override
    void processResults(ExecResult execResult) {
        ParseOutputStream streamProcessor = new ParseOutputStream()

        streamProcessor.processStream(outputStream.toString())

        String htmlDir = streamProcessor.htmlDir
        String coverage = streamProcessor.coverage


        FileUtils.copyDirectoryToDirectory(project.file(htmlDir), coverageOutputDir)

        CoverageXmlReporter coverageXmlReport = new CoverageXmlReporter(coverage)
        coverageReport.text = coverageXmlReport.generateXML()
    }

    static class ParseOutputStream {

        static final Pattern htmlPattern = Pattern.compile('^Coverage HTML written to dir (.*)$')
        static final Pattern xmlPattern = Pattern.compile('^Coverage XML written to file (.*)$')
        static final Pattern coveragePattern = Pattern.compile('^TOTAL ((\\s+\\d+)+)%$')

        String coverageXml;
        String htmlDir;
        String coverage;

        void processStream(String stream) {
            List<String> textOutput = stream.readLines()
            for (int i = textOutput.size() - 1; i >= 0; --i) {
                def xmlMatcher = xmlPattern.matcher(textOutput.get(i))
                def htmlMatcher = htmlPattern.matcher(textOutput.get(i));
                def coverageMatcher = coveragePattern.matcher(textOutput.get(i))

                if (xmlMatcher.matches()) {
                    coverageXml = xmlMatcher.group(1)
                } else if (htmlMatcher.matches()) {
                    htmlDir = htmlMatcher.group(1)
                } else if (coverageMatcher.matches()) {
                    coverage = coverageMatcher.group(1)
                }

                if (coverageXml != null && htmlDir != null && coverage != null) {
                    break;
                }
            }
        }
    }
}
