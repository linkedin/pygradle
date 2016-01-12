package com.linkedin.gradle.python.plugin.internal.python;

import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import org.gradle.api.Action;


public class SourceDistAction implements Action<SourceDistBinarySpec> {

  private final PythonEnvironment pythonEnvironment;

  public SourceDistAction(PythonEnvironment pythonEnvironment) {
    this.pythonEnvironment = pythonEnvironment;
  }

  @Override
  public void execute(SourceDistBinarySpec sourceDistBinarySpec) {
    sourceDistBinarySpec.setPythonEnvironment(pythonEnvironment);
  }
}
