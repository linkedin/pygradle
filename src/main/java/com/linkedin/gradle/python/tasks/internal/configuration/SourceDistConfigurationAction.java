package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.spec.binary.internal.SourceDistBinarySpecInternal;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;


public class SourceDistConfigurationAction extends DistConfigurationAction<BuildSourceDistTask> {

  private final SourceDistBinarySpecInternal spec;

  public SourceDistConfigurationAction(SourceDistBinarySpecInternal spec) {
    super(spec.getPythonEnvironment(), spec.getSources());
    this.spec = spec;
  }

  @Override
  public void doConfigure(BuildSourceDistTask task) {
    task.setOutputFormat(spec.getArtifactType());
  }
}
