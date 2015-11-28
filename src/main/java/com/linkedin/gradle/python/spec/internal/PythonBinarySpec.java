package com.linkedin.gradle.python.spec.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.Variant;

import java.io.File;


public interface PythonBinarySpec<T extends PythonComponentSpec> extends BinarySpec {

  @Variant
  PythonPlatform getTargetPlatform();

  void setTargetPlatform(PythonPlatform platform);

  PythonToolChain getToolChain();

  void setComponentSpec(T spec);

  T getComponentSpec();

  void setToolChain(PythonToolChain toolChain);

  void setVirtualEnvDir(File virtualEnvDir);

  File getVirtualEnvDir();

  void setPythonBuildDir(File pythonBuildDir);

  File getPythonBuildDir();
}
