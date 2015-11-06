package com.linkedin.gradle.python.spec;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import java.io.File;
import java.util.Collection;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.DependencySpec;
import org.gradle.platform.base.Variant;


public interface WheelBinarySpec extends BinarySpec {

  @Variant
  PythonPlatform getTargetPlatform();

  void setTargetPlatform(PythonPlatform platform);

  PythonToolChain getToolChain();

  void setToolChain(PythonToolChain toolChain);

  void setVirtualEnvDir(File virtualEnvDir);

  File getVirtualEnvDir();

  void setPythonBuildDir(File pythonBuildDir);

  File getPythonBuildDir();
}
