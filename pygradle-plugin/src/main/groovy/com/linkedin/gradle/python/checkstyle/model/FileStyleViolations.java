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

import java.util.ArrayList;
import java.util.List;


public class FileStyleViolations {
    private final String filename;
    private final List<StyleViolation> violations = new ArrayList<StyleViolation>();

    FileStyleViolations(String filename) {
        this.filename = filename;
    }

    void addViolation(Integer lineNumber, Integer columnNumber, String errorCode, String message) {
        violations.add(new StyleViolation(lineNumber, columnNumber, errorCode, message));
    }

    public String getFilename() {
        return filename;
    }

    public List<StyleViolation> getViolations() {
        return violations;
    }
}
