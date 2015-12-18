package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChainInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonToolChain implements PythonToolChainInternal {

  private final ExecActionFactory execActionFactory;
  private final PythonTargetPlatform targetPlatform;

  public DefaultPythonToolChain(ExecActionFactory execActionFactory, PythonTargetPlatform targetPlatform) {
    this.execActionFactory = execActionFactory;
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
  public DefaultPythonExecutable getSystemPythonExecutable() {
    return new DefaultPythonExecutable(execActionFactory, targetPlatform.getSystemPython());
  }

  @Override
  public PythonExecutable getLocalPythonExecutable(File pythonBuildDir) {
    return new DefaultPythonExecutable(execActionFactory, new File(pythonBuildDir, "bin/python"));
  }
}
