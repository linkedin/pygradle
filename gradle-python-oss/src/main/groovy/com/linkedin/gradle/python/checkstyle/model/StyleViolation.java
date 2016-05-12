package com.linkedin.gradle.python.checkstyle.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class StyleViolation {

  private final Integer lineNumber;
  private final Integer columnNumber;
  private final String errorNumber;
  private final String message;
  private final ViolationType violationType;

  public StyleViolation(Integer lineNumber, Integer columnNumber, String errorCode, String message) {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.errorNumber = errorCode;
    this.message = message;

    char errorTypeChar = errorCode.toUpperCase().charAt(0);
    switch (errorTypeChar) {
      case 'E':
        this.violationType = ViolationType.ERROR;
        break;
      case 'W':
        this.violationType = ViolationType.WARNING;
        break;
      case 'F':
        this.violationType = ViolationType.PY_FLAKES;
        break;
      case 'C':
        this.violationType = ViolationType.COMPLEXITY;
        break;
      case 'N':
        this.violationType = ViolationType.NAMING;
        break;
      default:
        this.violationType = ViolationType.OTHER;
    }
  }

  public Map<String, Object> createChecktyleMap() {
    HashMap<String, Object> checkstyleMap = new HashMap<String, Object>();
    checkstyleMap.put("line", getLineNumber());
    checkstyleMap.put("column", getColumnNumber());
    checkstyleMap.put("message", getErrorNumber() + " " + getMessage());
    checkstyleMap.put("severity", getViolationType().toString());
    return checkstyleMap;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public Integer getColumnNumber() {
    return columnNumber;
  }

  public String getErrorNumber() {
    return errorNumber;
  }

  public String getMessage() {
    return message;
  }

  public ViolationType getViolationType() {
    return violationType;
  }

  public enum ViolationType {
    ERROR,
    WARNING,
    PY_FLAKES,
    COMPLEXITY,
    NAMING,
    OTHER
  }
}
