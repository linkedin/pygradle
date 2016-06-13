package com.linkedin.gradle.python.extension

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.ExecSpec

class PythonVersionParser {

    public static String parsePythonVersion(Project project, File pythonInterpreter) {
        new ByteArrayOutputStream().withStream { output ->
            def execResult = project.exec { ExecSpec execSpec ->
                execSpec.executable = pythonInterpreter.getAbsolutePath()
                execSpec.args = ['--version']
                execSpec.standardOutput = output
                execSpec.errorOutput = output
                execSpec.ignoreExitValue = true
            }

            if(execResult.exitValue != 0) {
                throw new GradleException(output.toString())
            }

            return output.toString().trim().tokenize(' ')[1]
        }
    }
}
