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

import com.linkedin.gradle.python.extension.CliExtension
import com.linkedin.gradle.python.extension.DeployableExtension
import com.linkedin.gradle.python.util.ExtensionUtils
import groovy.transform.CompileStatic
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.StopActionException
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

/**
 * Generates completion files.
 */
@CompileStatic
class GenerateCompletionsTask extends AbstractPythonMainSourceDefaultTask {

    @OutputDirectory
    File getEtcDir() {
        def deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension)
        return deployableExtension.deployableEtcDir
    }

    @Override
    public void processResults(ExecResult execResult) {
        execResult.assertNormalExitValue()
    }

    public void preExecution() {
        if (!(ExtensionUtils.findPythonComponentExtension(project, CliExtension)?.generateCompletions)) {
            throw new StopActionException()
        }

        String completionScript = getClass().getResource('/templates/click_tabtab.py').text
        File.createTempFile('click_tabtab', '.py').with {
            deleteOnExit()
            write(completionScript)
            args(absolutePath)
        }
        getProject().file(getEtcDir()).mkdirs()
    }

    public void configureExecution(ExecSpec execSpec) {
        execSpec.setWorkingDir(getEtcDir())
    }
}
