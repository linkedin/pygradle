package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import org.gradle.api.Action;


public class WheelAction implements Action<WheelBinarySpec> {
  private final PythonEnvironment pythonEnvironment;

  public WheelAction(PythonEnvironment pythonEnvironment) {
    this.pythonEnvironment = pythonEnvironment;
  }

  @Override
  public void execute(WheelBinarySpec wheelBinarySpec) {
    wheelBinarySpec.setPythonEnvironment(pythonEnvironment);
  }
}
