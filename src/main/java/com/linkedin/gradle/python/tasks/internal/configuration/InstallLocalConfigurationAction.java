package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.tasks.InstallLocalProjectTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import java.io.File;


public class InstallLocalConfigurationAction extends BasePythonTaskAction<InstallLocalProjectTask> {
  private final String[] dependsOn;

  public InstallLocalConfigurationAction(File pythonBuildDir, File virtualEnvDir, PythonToolChain toolChain, String... dependsOn) {
    super(pythonBuildDir, virtualEnvDir, toolChain);
    this.dependsOn = dependsOn;
  }

  @Override
  public void configure(InstallLocalProjectTask task) {
    for (String taskName : dependsOn) {
      task.dependsOn(taskName);
    }
  }
}
