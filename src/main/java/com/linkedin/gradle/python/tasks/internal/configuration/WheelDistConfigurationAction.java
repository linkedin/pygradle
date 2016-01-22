package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.spec.binary.internal.WheelBinarySpecInternal;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.BuildWheelTask;


public class WheelDistConfigurationAction extends DistConfigurationAction<BuildWheelTask> {

  public WheelDistConfigurationAction(WheelBinarySpecInternal spec) {
    super(spec.getPythonEnvironment(), spec.getSources());
  }

  @Override
  public void doConfigure(BuildWheelTask task) {
    //NOOP
  }
}
