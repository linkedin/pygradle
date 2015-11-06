package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChainInternal;
import java.io.File;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonToolChain implements PythonToolChainInternal {

  private final ExecActionFactory execActionFactory;
  private final PythonVersion pythonVersion;
  private final OperatingSystem current;

  public DefaultPythonToolChain(ExecActionFactory execActionFactory, PythonVersion pythonVersion) {
    this.execActionFactory = execActionFactory;
    this.pythonVersion = pythonVersion;
    this.current = OperatingSystem.current();
  }

  @Override
  public String getDisplayName() {
    return String.format("Python %s", pythonVersion.getVersionString());
  }

  @Override
  public String getName() {
    return getDisplayName();
  }

  @Override
  public PythonVersion getVersion() {
    return pythonVersion;
  }

  @Override
  public DefaultPythonExecutable getPythonExecutable() {
    String name = "python" + getVersion().getVersionString();
    List<File> allInPath = current.findAllInPath(name);
    if(allInPath.isEmpty()) {
      throw new GradleException(String.format("Unable to find the '%s' executable. Tried the PATH.", name));
    }
    return new DefaultPythonExecutable(execActionFactory, allInPath.get(0));
  }

  @Override
  public PythonExecutable getLocalPythonExecutable(File pythonBuildDir) {
    return new DefaultPythonExecutable(execActionFactory, new File(pythonBuildDir, "bin/python"));
  }
}
