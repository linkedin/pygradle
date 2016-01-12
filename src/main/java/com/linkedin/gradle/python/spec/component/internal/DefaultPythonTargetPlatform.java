package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import org.gradle.util.GFileUtils;
import org.gradle.util.GUtil;
import org.gradle.util.VersionNumber;


public class DefaultPythonTargetPlatform implements PythonTargetPlatform {

  private static final Logger logger = Logging.getLogger(DefaultPythonTargetPlatform.class);

  private final PythonVersion version;
  private final File systemPython;

  public DefaultPythonTargetPlatform(OperatingSystem operatingSystem, String python) {
    if(new File(python).exists()) {
      systemPython = new File(python);
    } else if(python.startsWith("python")) {
      systemPython = operatingSystem.findInPath(python);
    } else {
      systemPython = operatingSystem.findInPath("python" + python);
    }

    if(systemPython == null) {
      throw new GradleException("Could not find " + python + " in PATH");
    } else if (!systemPython.canExecute()) {
      throw new GradleException("Unable to execute " + systemPython.getAbsolutePath());
    }

    String versionString;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(systemPython.getAbsolutePath(), "--version");
      processBuilder.redirectErrorStream(true);
      Process exec = processBuilder.start();
      ProcessGroovyMethods.waitForOrKill(exec, 5000);
      versionString = IOUtils.toString(exec.getInputStream());
      logger.debug("Python version for {} is {}", systemPython.getAbsolutePath(), versionString);
    } catch (IOException e) {
      logger.error("Unable to execute {}", systemPython.getAbsolutePath(), e);
      throw new GradleException("Unable to execute " + systemPython.getAbsolutePath());
    }

    String trimmedVersionString = StringUtils.trimToEmpty(versionString.split(" ")[1]);
    VersionNumber versionNumber = VersionNumber.parse(trimmedVersionString);
    String majorMinorString = String.format("%d.%d", versionNumber.getMajor(), versionNumber.getMinor());
    logger.debug("Python MAJOR.MINOR {}", majorMinorString);
    version = PythonVersion.parse(majorMinorString);
  }

  @Override
  public File getSystemPython() {
    return systemPython;
  }

  @Override
  public PythonVersion getVersion() {
    return version;
  }

  @Override
  public String getVersionAsString() {
    return getVersion().getVersionString();
  }

  @Override
  public String getDisplayName() {
    return String.format("Python %s", getVersion().getVersionString());
  }

  @Override
  public String getName() {
    return getDisplayName();
  }

  @Override
  public String toString() {
    return "DefaultPythonTargetPlatform{" +
        "version=" + version +
        ", systemPython=" + systemPython +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DefaultPythonTargetPlatform that = (DefaultPythonTargetPlatform) o;

    if (version != that.version) {
      return false;
    }
    return systemPython != null ? systemPython.equals(that.systemPython) : that.systemPython == null;
  }

  @Override
  public int hashCode() {
    int result = version != null ? version.hashCode() : 0;
    result = 31 * result + (systemPython != null ? systemPython.hashCode() : 0);
    return result;
  }
}
