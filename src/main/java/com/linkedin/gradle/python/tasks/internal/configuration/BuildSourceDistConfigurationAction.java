package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;

public class BuildSourceDistConfigurationAction extends BasePythonTaskAction<BuildSourceDistTask> {
    public BuildSourceDistConfigurationAction(PythonEnvironment pythonEnvironment) {
        super(pythonEnvironment);
    }

    @Override
    public void configure(BuildSourceDistTask task) {
        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
    }
}
