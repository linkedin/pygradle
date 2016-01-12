package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PyTestComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import org.gradle.platform.base.component.BaseComponentSpec;


public class DefaultPyTestComponentSpec extends BaseComponentSpec implements PyTestComponentSpec {

  private PythonComponentSpec componentSpec;
  private PythonTargetPlatform targetPlatform;

  @Override
  public PythonTargetPlatform getTargetPlatform() {
    return targetPlatform;
  }

  @Override
  public void setTargetPlatform(PythonTargetPlatform targetPlatform) {
    this.targetPlatform = targetPlatform;
  }

  @Override
  public PythonComponentSpec getTestedComponent() {
    return componentSpec;
  }

  @Override
  public void setTestedComponent(PythonComponentSpec componentSpec) {
    this.componentSpec = componentSpec;
  }
}
