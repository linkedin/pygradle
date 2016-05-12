package com.linkedin.gradle.python.tasks


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

    GenerateCompletionsTask() {
    }

    @OutputDirectory
    File getEtcDir() {
      return new File(component.deployableEtcDir)
    }

    @Override
    public void processResults(ExecResult execResult) {
        execResult.assertNormalExitValue();
    }

    public void preExecution() {
        if (!component.generateCompletions) {
            throw new StopActionException();
        }
        String completionScript = getClass().getResource('/templates/click_tabtab.py').text
        File.createTempFile('click_tabtab', '.py').with {
            deleteOnExit()
            write(completionScript)
            args(absolutePath)
        }
        getProject().file(component.deployableEtcDir).mkdirs();
    }

    public void configureExecution(ExecSpec execSpec) {
        execSpec.setWorkingDir(getEtcDir());
    }
}
