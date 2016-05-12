package com.linkedin.gradle.python.checkstyle.model;

import java.util.ArrayList;
import java.util.List;


public class FileStyleViolations {
  final String filename;
  final List<StyleViolation> violations = new ArrayList<StyleViolation>();

  public FileStyleViolations(String filename) {
    this.filename = filename;
  }

  public void addViolation(Integer lineNumber, Integer columnNumber, String errorCode, String message) {
    violations.add(new StyleViolation(lineNumber, columnNumber, errorCode, message));
  }
}
