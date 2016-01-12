package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.InstallLocalProjectTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;


public class InstallLocalConfigurationAction extends BasePythonTaskAction<InstallLocalProjectTask> {
  private final String[] dependsOn;

  public InstallLocalConfigurationAction(PythonEnvironment pythonEnvironment, String... dependsOn) {
    super(pythonEnvironment);
    this.dependsOn = dependsOn;
  }

  @Override
  public void configure(InstallLocalProjectTask task) {
    for (String taskName : dependsOn) {
      task.dependsOn(taskName);
    }
  }
}
