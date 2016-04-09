package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.plugin.internal.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;


public class DependencyConfigurationAction extends BasePythonTaskAction<InstallDependenciesTask> {

    private final PythonPluginConfigurations.PythonConfiguration configuration;
    private final String[] dependsOn;

    public DependencyConfigurationAction(PythonEnvironment pythonEnvironment,
                                         PythonPluginConfigurations.PythonConfiguration configuration,
                                         String... dependsOn) {
        super(pythonEnvironment);
        this.configuration = configuration;
        this.dependsOn = dependsOn;
    }

    @Override
    public void configure(InstallDependenciesTask task) {
        for (String taskName : dependsOn) {
            task.dependsOn(taskName);
        }
        task.setDependencyConfiguration(configuration.getConfiguration());
    }
}
