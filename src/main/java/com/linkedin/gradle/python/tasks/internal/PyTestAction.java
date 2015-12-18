package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.internal.SharedPythonInfrastructure;
import com.linkedin.gradle.python.tasks.PythonTestTask;


public class PyTestAction extends BasePythonTaskAction<PythonTestTask> {
  public PyTestAction(SharedPythonInfrastructure sharedPythonInfrastructure, PythonToolChain toolChain) {
    super(sharedPythonInfrastructure.getPythonBuildDir(), sharedPythonInfrastructure.getVirtualEnvDir(), toolChain);
  }

  @Override
  public void configure(PythonTestTask task) {

  }
}
