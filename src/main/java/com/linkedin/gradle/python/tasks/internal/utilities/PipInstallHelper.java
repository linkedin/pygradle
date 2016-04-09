package com.linkedin.gradle.python.tasks.internal.utilities;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.PipInstallExecAction;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;


public class PipInstallHelper {

    private static final Logger logger = Logging.getLogger(PipInstallHelper.class);

    private final PythonExecutable pythonExecutable;
    private final PipDependencyInstallAction action;

    public PipInstallHelper(PythonExecutable pythonExecutable, PipDependencyInstallAction action) {
        this.pythonExecutable = pythonExecutable;
        this.action = action;
    }

    public void install(File dependency) {
        execute(action.install(dependency));
    }

    public void forceInstall(File dependency) {
        execute(action.forceInstall(dependency));
    }

    public void execute(PipInstallExecAction install) {
        ExecResult execute = pythonExecutable.execute(install);
        if (execute.getExitValue() != 0) {
            logger.lifecycle(action.getWholeText());
            execute.assertNormalExitValue();
        }
    }

    public void uninstall(File file) {
        //todo(ethall)
        // Need to find a way to get the package name from the file name.
    }
}
