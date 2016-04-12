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

package com.linkedin.gradle.python.internal.platform;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PythonVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String matchAny = "(.*?)";
    private static final String optionalPython = "(python)?";
    private static final String versionNumber = "([23](\\.[0-9]+)*)";
    private static final Pattern PATTERN = Pattern.compile(matchAny + optionalPython + versionNumber + "$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final String version;
    private final String[] versionParts;

    PythonVersion(String version) {
        this.version = version;
        this.versionParts = version.split("\\.");
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
            return new PythonVersion(matcher.group(3));
        }

        throw new IllegalArgumentException("Unable to accept `" + value.toString() + "` as a PythonVersion");
    }

    public String getVersionString() {
        return version;
    }

    public String getMajorVersion() {
        return versionParts[0];
    }

    public String getMajorMinorVersion() {
        if (versionParts.length == 1) {
            return versionParts[0];
        } else {
            return versionParts[0] + "." + versionParts[1];
        }
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
