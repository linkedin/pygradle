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
