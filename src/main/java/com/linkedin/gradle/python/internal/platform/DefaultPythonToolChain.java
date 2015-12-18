package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChainInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;


public class DefaultPythonToolChain implements PythonToolChainInternal {

  private final PythonTargetPlatform targetPlatform;

  public DefaultPythonToolChain(PythonTargetPlatform targetPlatform) {
    this.targetPlatform = targetPlatform;
  }

  @Override
  public String getDisplayName() {
    return String.format("Python %s", targetPlatform.getVersion().getVersionString());
  }

  @Override
  public String getName() {
    return getDisplayName();
  }

  @Override
  public PythonVersion getVersion() {
    return targetPlatform.getVersion();
  }

  @Override
  public PythonExecutable getSystemPythonExecutable() {
    return targetPlatform.getSystemPythonExecutable();
  }

  @Override
  public PythonExecutable getLocalPythonExecutable(File pythonBuildDir) {
//    return targetPlatform.getPythonExecutableFromVenv(pythonBuildDir);
    return null;
  }
}
