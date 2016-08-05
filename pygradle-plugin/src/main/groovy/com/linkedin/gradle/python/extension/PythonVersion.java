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

public class PythonVersion {

    private final String version;

    public PythonVersion(String version) {
        this.version = version;
    }

    /**
     * The exact version of Python this project uses, such as '2.7.11'.
     */
    public String getPythonVersion() {
        return version;
    }

    /**
     * The short version of Python this project uses, such as '2.7'.
     */
    public String getPythonMajorMinor() {
        String[] split = version.split("\\.");
        return String.format("%s.%s", split[0], split[1]);
    }

    /**
     * The major version of Python this project uses, such as '2'.
     */
    public String getPythonMajor() {
        return version.split("\\.")[0];
    }
}
