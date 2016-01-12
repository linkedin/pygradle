package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;

public class PyTestConfigurationAction extends BasePythonTaskAction<PythonTestTask> {

    public PyTestConfigurationAction(PythonEnvironment pythonEnvironment) {
        super(pythonEnvironment);
    }

    @Override
    public void configure(PythonTestTask task) {

    }
}