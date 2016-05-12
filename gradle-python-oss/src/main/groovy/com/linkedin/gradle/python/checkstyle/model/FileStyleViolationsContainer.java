package com.linkedin.gradle.python.checkstyle.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


/**
 * This will contain all of the violations, handling adding violations to existing files, and adding new files.
 */
public class FileStyleViolationsContainer {
  private static final Logger log = Logging.getLogger(FileStyleViolationsContainer.class);

  //Flake8 patten
  Pattern flake8Pattern = Pattern.compile("(.*?):(\\d+):(\\d+): ([A-Z]\\d{3}) (.*)");

  Map<String, FileStyleViolations> violationMap = new HashMap<String, FileStyleViolations>();

  public void parseLine(String line) {
    Matcher matcher = flake8Pattern.matcher(line);
    if(matcher.find()) {
      String fileName = matcher.group(1);
      Integer lineNumber = Integer.valueOf(matcher.group(2));
      Integer columnNumber = Integer.valueOf(matcher.group(3));
      String errorCode = matcher.group(4);
      String message = matcher.group(5);

      //If there isn't a violation for this file, create an empty one
      if(!violationMap.containsKey(fileName)) {
        violationMap.put(fileName, new FileStyleViolations(fileName));
      }

      //Add violation to existing file
      violationMap.get(fileName).addViolation(lineNumber, columnNumber, errorCode, message);
    } else {
      //Logging when something doesn't work
      log.info("Unable to parse `{}`", line);
    }
  }

  public Collection<FileStyleViolations> getViolations() {
    return violationMap.values();
  }
}
