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
package com.linkedin.gradle.python.extension;

import org.gradle.api.GradleException;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;


public class PythonDefaultVersions implements Serializable {
    private final String defaultPython2Version;
    private final String defaultPython3Version;
    private final Collection<String> allowedVersions;

    public PythonDefaultVersions(String defaultPython2, String defaultPython3, Collection<String> allowedVersions) {
        defaultPython2Version = defaultPython2;
        defaultPython3Version = defaultPython3;
        this.allowedVersions = allowedVersions;
    }

    public PythonDefaultVersions(Collection<String> allowedVersions) {
        defaultPython2Version = "2.7";
        defaultPython3Version = "3.7";
        this.allowedVersions = allowedVersions;
    }

    public PythonDefaultVersions(String defaultPython2, String defaultPython3) {
        this(defaultPython2, defaultPython3, new TreeSet<>());
    }

    public PythonDefaultVersions() {
        this(new TreeSet<>());
    }

    public String normalize(String version) {
        if (version.equals("2")) {
            return defaultPython2Version;
        }
        if (version.equals("3")) {
            return defaultPython3Version;
        }
        if (allowedVersions.isEmpty()) {
            // All versions are allowed.
            return version;
        }
        if (!allowedVersions.contains(new PythonVersion(version).getPythonMajorMinor())) {
            throw new GradleException(
                "Python " + version + " is not allowed; choose from " + allowedVersions
                    + "\nSee https://github.com/linkedin/pygradle/blob/master/docs/plugins/python.md"
                    + "#default-and-allowed-python-version");
        }
        return version;
    }
}
