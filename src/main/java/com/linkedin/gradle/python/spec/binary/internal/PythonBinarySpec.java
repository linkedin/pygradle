package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import java.io.File;
import org.gradle.platform.base.BinarySpec;


public interface PythonBinarySpec extends BinarySpec {

  void setPythonEnvironment(PythonEnvironment pythonEnvironment);

  PythonEnvironment getPythonEnvironment();
}
