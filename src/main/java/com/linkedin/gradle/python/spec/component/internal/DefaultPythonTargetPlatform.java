package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChainInternal;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonTargetPlatform implements PythonTargetPlatform, PythonToolChainInternal {

  private final ExecActionFactory execActionFactory;
  private final PythonVersion version;
  private final File systemPython;

  DefaultPythonTargetPlatform(ExecActionFactory execActionFactory, OperatingSystem operatingSystem, String python) {
    this.execActionFactory = execActionFactory;

    if(new File(python).exists()) {
      systemPython = new File(python);
    } else if(python.startsWith("python")) {
      systemPython = operatingSystem.findInPath(python);
    } else {
      systemPython = operatingSystem.findInPath("python" + python);
    }

    if(systemPython == null) {
      throw new GradleException("Could not find " + python + " in PATH");
    } else if (!systemPython.canExecute()){
      throw new GradleException("Unable to execute " + systemPython.getAbsolutePath());
    }

    version = PythonVersion.parse(systemPython.getName().substring("python".length()));
  }

  @Override
  public PythonExecutable getSystemPythonExecutable() {
    return new DefaultPythonExecutable(execActionFactory, systemPython);
  }

  @Override
  public PythonExecutable getLocalPythonExecutable(File venvDir) {
    return new DefaultPythonExecutable(execActionFactory, new File(venvDir, "bin/python"));
  }

  @Override
  public PythonVersion getVersion() {
    return version;
  }

  @Override
  public String getDisplayName() {
    return String.format("Python %s", getVersion().getVersionString());
  }

  @Override
  public String getName() {
    return getDisplayName();
  }
}
