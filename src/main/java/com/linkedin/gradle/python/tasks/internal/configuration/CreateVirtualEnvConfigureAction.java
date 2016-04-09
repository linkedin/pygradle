package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.plugin.internal.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;


public class CreateVirtualEnvConfigureAction extends BasePythonTaskAction<VirtualEnvironmentBuild> {
    private final PythonPluginConfigurations.PythonConfiguration bootstrapConfiguration;

    public CreateVirtualEnvConfigureAction(final PythonEnvironment pythonEnvironment,
                                           final PythonPluginConfigurations.PythonConfiguration bootstrapConfiguration) {
        super(pythonEnvironment);
        this.bootstrapConfiguration = bootstrapConfiguration;
    }

    @Override
    public void configure(VirtualEnvironmentBuild task) {
        task.setVirtualEnvFiles(bootstrapConfiguration.getConfiguration());

        String environmentName = getPythonEnvironment().getEnvironmentName();
        String versionString = getPythonEnvironment().getVersion().getVersionString();

        task.setActivateScriptName(String.format("activate-%s-%s", environmentName, versionString));
        task.setVirtualEnvName(String.format("(%s-%s)", environmentName, versionString));
    }
}
