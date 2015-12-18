package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import java.io.File;


public class DependencyConfigurationAction extends BasePythonTaskAction<InstallDependenciesTask> {

  private final PythonPluginConfigurations.PythonConfiguration configuration;
  private final String[] dependsOn;

  public DependencyConfigurationAction(File pythonBuildDir,
                                       File virtualEnvDir,
                                       PythonToolChain toolChain,
                                       PythonPluginConfigurations.PythonConfiguration configuration,
                                       String... dependsOn) {
    super(pythonBuildDir, virtualEnvDir, toolChain);
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