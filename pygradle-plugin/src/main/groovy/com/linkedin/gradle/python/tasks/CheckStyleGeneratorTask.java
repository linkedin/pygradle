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
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.checkstyle.CheckStyleXmlReporter;
import com.linkedin.gradle.python.checkstyle.model.FileStyleViolationsContainer;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * This has slightly different behavior from {@link Flake8Task} in that, if the inputs fail violation this will continue.
 * <p>
 * This was done so that a report will be generated vs stopping the gradle process and never generating reports.
 */
public class CheckStyleGeneratorTask extends Flake8Task {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private File checkstyleReport = new File(getProject().getBuildDir(), "/checkstyle.xml");

    public CheckStyleGeneratorTask() {
        stdOut = outputStream;
        errOut = outputStream;
        ignoreExitValue = true;
    }

    @Override
    public void processResults(ExecResult execResult) {
        final FileStyleViolationsContainer container = new FileStyleViolationsContainer();

        String[] lines = outputStream.toString().split("\\r?\\n");
        for (String line : lines) {
            container.parseLine(line);
        }

        CheckStyleXmlReporter reporter = new CheckStyleXmlReporter(container);
        try {
            FileUtils.write(checkstyleReport, reporter.generateXml());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @OutputFile
    public File getCheckstyleReport() {
        return checkstyleReport;
    }

    public void setCheckstyleReport(File checkstyleReport) {
        this.checkstyleReport = checkstyleReport;
    }
}
