package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.internal.DefaultPythonTaskRule;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;

import java.io.File;

public class BuildSourceDistConfigurationAction extends BasePythonTaskAction<BuildSourceDistTask> {
    public BuildSourceDistConfigurationAction(File pythonBuildDir, File virtualEnvDir, PythonToolChain toolChain) {
        super(pythonBuildDir, virtualEnvDir, toolChain);
    }

    @Override
    public void configure(BuildSourceDistTask task) {
        task.dependsOn(DefaultPythonTaskRule.projectSetupTaskName(getToolChain().getVersion().getVersionString()));
    }
}
