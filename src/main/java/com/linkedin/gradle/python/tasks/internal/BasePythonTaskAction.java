package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.tasks.BasePythonTask;
import org.gradle.api.Action;

abstract public class BasePythonTaskAction<T extends BasePythonTask> implements Action<T> {

    private final PythonBinarySpec binary;

    public BasePythonTaskAction(PythonBinarySpec binary) {
        this.binary = binary;
    }

    @Override
    public void execute(T task) {
        task.setPythonToolChain(binary.getToolChain());
        task.setPythonBuilDir(binary.getPythonBuildDir());
        task.setVenvDir(binary.getVirtualEnvDir());
        configure(task);
    }

    public abstract void configure(T task);

}
