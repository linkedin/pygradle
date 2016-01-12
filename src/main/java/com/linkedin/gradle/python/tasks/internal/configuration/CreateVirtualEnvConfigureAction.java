package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
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
    task.setActivateScriptName(String.format("activate-%s", getPythonEnvironment().getVersion().getVersionString()));
  }
}
