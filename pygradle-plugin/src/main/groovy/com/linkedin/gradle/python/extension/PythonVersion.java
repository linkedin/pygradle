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

import java.io.Serializable;


public class PythonVersion implements Serializable {

    private final String version;

    public PythonVersion(String version) {
        this.version = version;
    }

    private String getVersionPart(int position) {
        String[] parts = version.split("\\.");
        return parts[position];
    }

    /**
     * @return The exact version of Python this project uses, such as '2.7.11'.
     */
    public String getPythonVersion() {
        return version;
    }

    /**
     * @return The short version of Python this project uses, such as '2.7'.
     */
    public String getPythonMajorMinor() {
        return String.format("%s.%s", getVersionPart(0), getVersionPart(1));
    }

    /**
     * @return The major version of Python this project uses, such as '2'.
     */
    public String getPythonMajor() {
        return getVersionPart(0);
    }

    /**
     * @return The minor version of Python this project uses, such as '7'.
     */
    public String getPythonMinor() {
        return getVersionPart(1);
    }

    /**
     * @return The patch version of Python this project uses, such as '11'.
     */
    public String getPythonPatch() {
        return getVersionPart(2);
    }

    @Override
    public String toString() {
        return "PythonVersion{"
            + "version='" + version + '\''
            + '}';
    }
}
