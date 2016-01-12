package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import java.io.File;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonEnvironment implements PythonEnvironment {

  final DefaultPythonExecutable systemPythonExecutable;
  final DefaultPythonExecutable virtualEnvExecutable;
  final File venvDir;
  final File vendorDir;
  final File buildDir;
  final PythonVersion version;
  final String name;

  public DefaultPythonEnvironment(File pythonExecutable, PythonVersion version, File buildDir, ExecActionFactory execActionFactory, String name) {
    this.systemPythonExecutable = new DefaultPythonExecutable(execActionFactory, pythonExecutable);
    this.version = version;
    this.buildDir = buildDir;
    this.name = name;
    this.venvDir = new File(buildDir, String.format("python-%s-%s/venv", version.getVersionString(), name));
    this.vendorDir = new File(buildDir, String.format("python-%s-%s/vendor", version.getVersionString(), name));
    this.virtualEnvExecutable = new DefaultPythonExecutable(execActionFactory, new File(venvDir, "bin/python"));
  }

  @Override
  public String getEnvironmentSetupTaskName() {
    return String.format("setup%s", version.getVersionString());
  }

  @Override
  public PythonExecutable getVirtualEnvPythonExecutable() {
    return virtualEnvExecutable;
  }

  @Override
  public PythonExecutable getSystemPythonExecutable() {
    return systemPythonExecutable;
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

  @Override
  public File getBuildDir() {
    return buildDir;
  }

  @Override
  public String getEnvironmentName() {
    return name;
  }
}
