package com.linkedin.gradle.python.internal.toolchain;

import org.gradle.api.Action;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;


public class DefaultPythonExecutable implements PythonExecutable {

    private final ExecActionFactory execActionFactory;
    private final File pythonPath;

    public DefaultPythonExecutable(ExecActionFactory execActionFactory, File file) {
        this.execActionFactory = execActionFactory;
        this.pythonPath = file.getAbsoluteFile();
    }

    @Override
    public File getPythonPath() {
        return pythonPath;
    }

    @Override
    public ExecResult execute(Action<ExecAction> execActionAction) {
        ExecAction execAction = execActionFactory.newExecAction();
        execAction.executable(pythonPath);
        execActionAction.execute(execAction);
        return execAction.execute();
    }
}
