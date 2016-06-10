package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.extension.CliExtension
import com.linkedin.gradle.python.extension.DeployableExtension
import com.linkedin.gradle.python.util.ExtensionUtils
import groovy.transform.CompileStatic
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.StopActionException
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec


/**
 * Generates completion files.
 */
@CompileStatic
class GenerateCompletionsTask extends AbstractPythonMainSourceDefaultTask {

    GenerateCompletionsTask() {
    }

    @OutputDirectory
    File getEtcDir() {
        def deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension)
        return deployableExtension.deployableEtcDir
    }

    @Override
    public void processResults(ExecResult execResult) {
        execResult.assertNormalExitValue();
    }

    public void preExecution() {
        if (!ExtensionUtils.findPythonComponentExtension(project, CliExtension)) {
            throw new StopActionException();
        }
        String completionScript = getClass().getResource('/templates/click_tabtab.py').text
        File.createTempFile('click_tabtab', '.py').with {
            deleteOnExit()
            write(completionScript)
            args(absolutePath)
        }
        getProject().file(getEtcDir()).mkdirs();
    }

    public void configureExecution(ExecSpec execSpec) {
        execSpec.setWorkingDir(getEtcDir());
    }
}
