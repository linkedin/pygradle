package com.linkedin.gradle.python.extension;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

public class PythonVersionParser {

    private PythonVersionParser() {
        //NOOP
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

        String versionString = output.split(" ")[1];
        return new PythonVersion(versionString);
    }
}
