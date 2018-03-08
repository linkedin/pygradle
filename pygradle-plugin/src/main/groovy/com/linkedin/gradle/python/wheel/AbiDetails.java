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
package com.linkedin.gradle.python.wheel;

import java.io.File;
import java.io.Serializable;

public class AbiDetails implements Serializable {

    private final File pythonExecutable;
    private final String pythonTag;
    private final String abiTag;
    private final String platformTag;

    public AbiDetails(File pythonExecutable, String pythonTag, String abiTag, String platformTag) {
        this.pythonExecutable = pythonExecutable;
        this.pythonTag = pythonTag;
        this.abiTag = abiTag;
        this.platformTag = platformTag;
    }

    public String getPythonTag() {
        return pythonTag;
    }

    public String getAbiTag() {
        return abiTag;
    }

    public String getPlatformTag() {
        return platformTag;
    }

    public File getPythonExecutable() {
        return pythonExecutable;
    }

    @Override
    public String toString() {
        return "AbiDetails{"
            + "pythonExec='" + pythonExecutable.toString() + '\''
            + ", pythonTag='" + pythonTag + '\''
            + ", abiTag='" + abiTag + '\''
            + ", platformTag='" + platformTag + '\''
            + '}';
    }
}
