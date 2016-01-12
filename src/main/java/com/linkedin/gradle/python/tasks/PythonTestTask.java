package com.linkedin.gradle.python.tasks;

import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;

public class PythonTestTask extends BasePythonTask {

    @TaskAction
    public void runTests() {
        final File pyTest = new File(getVenvDir(), "bin/py.test");
        if(!pyTest.exists()) {
            throw new PyTestNotFoundException();
        }
        execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.args(pyTest.getAbsoluteFile());
                execAction.args(String.format("--ignore=%s", getPythonEnvironment().getVenvDir().getAbsolutePath()));
            }
        });
    }

    public static class PyTestNotFoundException extends RuntimeException {
    }
}
