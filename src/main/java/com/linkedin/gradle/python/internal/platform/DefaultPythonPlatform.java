package com.linkedin.gradle.python.internal.platform;

public class DefaultPythonPlatform implements PythonPlatform {

  private final String name;
  private final PythonVersion pythonVersion;

  public DefaultPythonPlatform(PythonVersion pythonVersion) {
    this.name = generateName(pythonVersion);
    this.pythonVersion = pythonVersion;
  }

  @Override
  public String getDisplayName() {
    return String.format("Python %s");
  }

  @Override
  public String getName() {
    return name;
  }

  private static String generateName(PythonVersion pythonVersion) {
    return "python" + pythonVersion.getVersionString();
  }

  @Override
  public PythonVersion getVersion() {
    return pythonVersion;
  }

  public static DefaultPythonPlatform current() {
    return new DefaultPythonPlatform(PythonVersion.VERSION_2_6);
  }
}
