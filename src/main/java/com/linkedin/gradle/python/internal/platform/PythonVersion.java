package com.linkedin.gradle.python.internal.platform;

public enum PythonVersion {
  VERSION_2_6("2.6"),
  VERSION_2_7("2.7"),
  VERSION_3_0("3.0"),
  VERSION_3_1("3.1"),
  VERSION_3_2("3.2"),
  VERSION_3_3("3.3"),
  VERSION_3_4("3.4"),
  VERSION_3_5("3.5");

  private final String versionString;

  PythonVersion(String versionString) {
    this.versionString = versionString;
  }

  public static PythonVersion parse(String string) {
    return toVersion(string);
  }

  public static PythonVersion toVersion(Object value) {
    if (null == value) {
      return null;
    }

    if (value instanceof PythonVersion) {
      return (PythonVersion) value;
    }

    String name = value.toString();
    return valueOf(String.format("VERSION_%s", name.replace('.', '_')));
  }

  public String getVersionString() {
    return versionString;
  }

  public String getMajorVersion() {
    return versionString.substring(0, 1);
  }
}
