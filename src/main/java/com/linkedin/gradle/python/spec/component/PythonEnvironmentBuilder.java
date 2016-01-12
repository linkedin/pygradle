package com.linkedin.gradle.python.spec.component;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.component.internal.DefaultPythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.VersionNumber;


public class PythonEnvironmentBuilder {

  private static final Logger logger = Logging.getLogger(PythonEnvironmentBuilder.class);

  private ExecActionFactory execActionFactory;
  private File pythonExecutable;
  private File buildDir;
  private String name;

  public PythonEnvironmentBuilder(String python) {
    if (new File(python).exists()) {
      pythonExecutable = new File(python);
    } else if (python.startsWith("python")) {
      pythonExecutable = OperatingSystem.current().findInPath(python);
    } else {
      pythonExecutable = OperatingSystem.current().findInPath("python" + python);
    }
  }

  public PythonEnvironmentBuilder withBuildDir(File buildDir) {
    this.buildDir = buildDir;
    return this;
  }

  public PythonEnvironmentBuilder withExecActionFactory(ExecActionFactory execActionFactory) {
    this.execActionFactory = execActionFactory;
    return this;
  }

  public PythonEnvironmentBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public PythonEnvironment build() {
    PythonVersion version = getVersion();
    Objects.requireNonNull(pythonExecutable, "Python executable should be non-null");
    Objects.requireNonNull(version, "Python version should be non-null");
    Objects.requireNonNull(buildDir, "Build Dir should be non-null");
    Objects.requireNonNull(execActionFactory, "ExecActionFactory should be non-null");
    Objects.requireNonNull(name, "Name should be non-null");
    return new DefaultPythonEnvironment(pythonExecutable, version, buildDir, execActionFactory, name);
  }

  private PythonVersion getVersion() {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable.getAbsolutePath(), "--version");
      processBuilder.redirectErrorStream(true);
      Process exec = processBuilder.start();
      ProcessGroovyMethods.waitForOrKill(exec, 5000);
      String versionString = IOUtils.toString(exec.getInputStream());
      logger.debug("Python version for {} is {}", pythonExecutable.getAbsolutePath(), versionString);

      String trimmedVersionString = StringUtils.trimToEmpty(versionString.split(" ")[1]);
      VersionNumber versionNumber = VersionNumber.parse(trimmedVersionString);
      String majorMinorString = String.format("%d.%d", versionNumber.getMajor(), versionNumber.getMinor());
      logger.debug("Python MAJOR.MINOR {}", majorMinorString);
      return PythonVersion.parse(majorMinorString);
    } catch (IOException e) {
      logger.error("Unable to execute {}", pythonExecutable.getAbsolutePath(), e);
      throw new GradleException("Unable to execute " + pythonExecutable.getAbsolutePath());
    }
  }
}
