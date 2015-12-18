package com.linkedin.gradle.python.spec.binary.internal;

import java.io.File;
import org.gradle.platform.base.BinarySpec;


public interface PythonBinarySpec extends BinarySpec {
  void setBuildDir(File buildDir);

  File getBuildDir();
}
