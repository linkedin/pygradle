package com.linkedin.gradle.python.extension;

public class PythonVersion {

  private final String version;

  public PythonVersion(String version) {
    this.version = version;
  }

  /**
   * The exact version of Python this project uses, such as '2.7.11'.
   */
  public String getPythonVersion() {
    return version;
  }

  /**
   * The short version of Python this project uses, such as '2.7'.
   */
  public String getPythonMajorMinor() {
    String[] split = version.split("\\.");
    return String.format("%s.%s", split[0], split[1]);
  }

  /**
   * The major version of Python this project uses, such as '2'.
   */
  public String getPythonMajor() {
    return version.split("\\.")[0];
  }
}
