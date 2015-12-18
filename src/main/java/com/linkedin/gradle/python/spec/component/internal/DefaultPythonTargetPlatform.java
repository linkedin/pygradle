package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.internal.os.OperatingSystem;


public class DefaultPythonTargetPlatform implements PythonTargetPlatform {

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
    } else if (!systemPython.canExecute()){
      throw new GradleException("Unable to execute " + systemPython.getAbsolutePath());
    }

    version = PythonVersion.parse(systemPython.getName().substring("python".length()));
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
