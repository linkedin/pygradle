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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.gradle.api.GradleException;


public class PythonDefaults {
    private final String defaultPython2Version;
    private final String defaultPython3Version;
    private final Collection<String> allowedVersions;

    public PythonDefaults(String defaultPython2, String defaultPython3, Collection<String> allowedVersions) {
        defaultPython2Version = defaultPython2;
        defaultPython3Version = defaultPython3;
        this.allowedVersions = allowedVersions;
    }

    public PythonDefaults(String defaultPython2, String defaultPython3) {
        this(defaultPython2, defaultPython3, new HashSet<>(Arrays.asList("2.6", "2.7", "3.4", "3.5", "3.6")));
    }

    public PythonDefaults() {
        this("2.6", "3.5");
    }

    public String normalize(String version) {
        if (version.equals("2")) {
            return defaultPython2Version;
        }
        if (version.equals("3")) {
            return defaultPython3Version;
        }
        if (!allowedVersions.contains(new PythonVersion(version).getPythonMajorMinor())) {
            throw new GradleException("Python version not allowed: " + version);
        }
        return version;
    }
}
