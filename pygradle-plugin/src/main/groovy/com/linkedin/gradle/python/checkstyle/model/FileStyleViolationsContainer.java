/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.checkstyle.model;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        if (matcher.find()) {
            String fileName = matcher.group(1);
            Integer lineNumber = Integer.valueOf(matcher.group(2));
            Integer columnNumber = Integer.valueOf(matcher.group(3));
            String errorCode = matcher.group(4);
            String message = matcher.group(5);

            //If there isn't a violation for this file, create an empty one
            if (!violationMap.containsKey(fileName)) {
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
