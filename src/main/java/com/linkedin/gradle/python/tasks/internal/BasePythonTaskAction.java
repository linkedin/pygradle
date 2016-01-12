package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.BasePythonTask;
import org.gradle.api.Action;

abstract public class BasePythonTaskAction<T extends BasePythonTask> implements Action<T> {

    private final PythonEnvironment pythonEnvironment;

    protected BasePythonTaskAction(PythonEnvironment pythonEnvironment) {
        this.pythonEnvironment = pythonEnvironment;
    }

    @Override
    public void execute(T task) {
        task.setPythonEnvironment(pythonEnvironment);
        configure(task);
    }

    public PythonEnvironment getPythonEnvironment() {
        return pythonEnvironment;
    }

    public abstract void configure(T task);

}
