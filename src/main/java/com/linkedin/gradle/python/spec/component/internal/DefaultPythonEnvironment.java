package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import java.io.File;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonEnvironment implements PythonEnvironment {

  final DefaultPythonExecutable pythonExecutable;
  final File venvDir;
  final File vendorDir;
  final PythonVersion version;

  public DefaultPythonEnvironment(File pythonExecutable, PythonVersion version, File buildDir, ExecActionFactory execActionFactory) {
    this.pythonExecutable = new DefaultPythonExecutable(execActionFactory, pythonExecutable);
    this.version = version;
    this.venvDir = new File(buildDir, String.format("python-%s/venv", version.getVersionString()));
    this.vendorDir = new File(buildDir, String.format("python-%s/vendor", version.getVersionString()));
  }

  @Override
  public String getEnvironmentSetupTaskName() {
    return String.format("setup%s", version.getVersionString());
  }

  @Override
  public PythonExecutable getPythonExecutable() {
    return pythonExecutable;
  }

  @Override
  public File getVenvDir() {
    return venvDir;
  }

  @Override
  public PythonVersion getVersion() {
    return version;
  }

  @Override
  public File getVendorDir() {
    return vendorDir;
  }
}
