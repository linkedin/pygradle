package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.tasks.BasePythonTask;
import org.gradle.api.Action;

import java.io.File;

abstract public class BasePythonTaskAction<T extends BasePythonTask> implements Action<T> {

    private final File pythonBuildDir;
    private final File virtualEnvDir;
    private final PythonToolChain toolChain;

    protected BasePythonTaskAction(File pythonBuildDir, File virtualEnvDir, PythonToolChain toolChain) {
        this.pythonBuildDir = pythonBuildDir;
        this.virtualEnvDir = virtualEnvDir;
        this.toolChain = toolChain;
    }

    @Override
    public void execute(T task) {
        task.setPythonToolChain(toolChain);
        task.setPythonBuilDir(pythonBuildDir);
        task.setVenvDir(virtualEnvDir);
        configure(task);
    }

    public abstract void configure(T task);

}
