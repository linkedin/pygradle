package com.linkedin.gradle.python.extension


import org.gradle.api.Project
import org.gradle.process.ExecSpec

class PythonVersionParser {

    public static String parsePythonVersion(Project project, File pythonInterpreter) {
        new ByteArrayOutputStream().withStream { output ->
            project.exec { ExecSpec execSpec ->
                execSpec.executable = pythonInterpreter.getAbsolutePath()
                execSpec.args = ['--version']
                execSpec.standardOutput = output
                execSpec.errorOutput = output
                execSpec.ignoreExitValue = true
            }
            return output.toString().trim().tokenize(' ')[1]
        }
    }
}
