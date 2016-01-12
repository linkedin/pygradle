package com.linkedin.gradle.python.spec.component;

import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import org.gradle.platform.base.test.TestSuiteSpec;


public interface PyTestComponentSpec extends TestSuiteSpec {

  PythonTargetPlatform getTargetPlatform();

  void setTargetPlatform(PythonTargetPlatform targetPlatform);

  PythonComponentSpec getTestedComponent();

  void setTestedComponent(PythonComponentSpec componentSpec);
}
