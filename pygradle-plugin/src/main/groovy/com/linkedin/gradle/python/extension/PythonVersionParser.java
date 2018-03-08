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
package com.linkedin.gradle.python.extension;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

public class PythonVersionParser {

    private PythonVersionParser() {
        // noop
    }

    public static PythonVersion parsePythonVersion(final Project project, final File pythonInterpreter) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult execResult = project.exec(execSpec -> {
            execSpec.setExecutable(pythonInterpreter.getAbsolutePath());
            execSpec.setArgs(Collections.singletonList("--version"));
            execSpec.setStandardOutput(outputStream);
            execSpec.setErrorOutput(outputStream);
            execSpec.setIgnoreExitValue(true);
        });

        String output = outputStream.toString();
        if (execResult.getExitValue() != 0) {
            throw new GradleException(output);
        }

        String versionString = output.trim().split(" ")[1];
        return new PythonVersion(versionString);
    }
}
