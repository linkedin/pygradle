package com.linkedin.gradle.python.internal.platform;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PythonVersion {

  private static final Pattern PATTERN = Pattern.compile(".*?(python)?([23][0-9\\.]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private final String version;

  PythonVersion(String version) {
    this.version = version;
  }

  public static PythonVersion parse(String string) {
    return toVersion(string);
  }

  public static PythonVersion toVersion(Object value) {
    if (null == value) {
      throw new IllegalArgumentException("PythonVersion cannot be null!");
    }

    if (value instanceof PythonVersion) {
      return (PythonVersion) value;
    }

    Matcher matcher = PATTERN.matcher(value.toString());
    if (matcher.find()) {
      return new PythonVersion(matcher.group(2));
    }

    throw new IllegalArgumentException("Unable to accept " + value.toString() + " as a PythonVersion");
  }

  public String getVersionString() {
    return version;
  }

  public String getMajorVersion() {
    return version.substring(0, 1);
  }

  public String getMajorMinorVersion() {
    return version.substring(3);
  }

  @Override
  public String toString() {
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PythonVersion that = (PythonVersion) o;
    return Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version);
  }
}
