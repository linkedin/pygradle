package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import org.gradle.api.Action;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;

import java.io.File;

public class PythonTestTask extends BasePythonTask {

    @TaskAction
    public void runTests() {
        final File pyTest = new File(venvDir, "bin/py.test");
        if(!pyTest.exists()) {
            throw new PyTestNotFoundException();
        }
        final PythonExecutable pythonExecutable = getPythonToolChain().getLocalPythonExecutable(venvDir);
        pythonExecutable.execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.args(pyTest.getAbsoluteFile());
                execAction.args(String.format("--ignore=%s", getPythonBuilDir().getAbsolutePath()));
            }
        });
    }

    public static class PyTestNotFoundException extends RuntimeException {
    }
}
