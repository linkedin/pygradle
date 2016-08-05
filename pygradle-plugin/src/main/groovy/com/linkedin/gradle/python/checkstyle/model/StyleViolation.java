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

import java.util.HashMap;
import java.util.Map;


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

        char errorTypeChar = errorCode != null ? errorCode.toUpperCase().charAt(0) : 'X';
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
                break;
        }
    }

    public Map<String, Object> createCheckstyleMap() {
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
