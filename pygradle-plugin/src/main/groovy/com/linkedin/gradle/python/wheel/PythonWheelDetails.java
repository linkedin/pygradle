/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.wheel;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonWheelDetails {

    // PEP-0427
    public static final String WHEEL_FILE_FORMAT = "(?<dist>.*?)-(?<version>.*?)(-(.*?))?-(?<pythonTag>.*?)-(?<abiTag>.*?)-(?<platformTag>.*?).whl";

    final File file;
    final String dist;
    final String version;
    final String pythonTag;
    final String abiTag;
    final String platformTag;

    public PythonWheelDetails(File file, Matcher matcher) {
        this.file = file;
        this.dist = matcher.group("dist");
        this.version = matcher.group("version");
        this.pythonTag = matcher.group("pythonTag");
        this.abiTag = matcher.group("abiTag");
        this.platformTag = matcher.group("platformTag");
    }

    public String getDist() {
        return dist;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "PythonWheelDetails{"
            + "file=" + file
            + ", dist='" + dist + '\''
            + ", version='" + version + '\''
            + ", pythonTag='" + pythonTag + '\''
            + ", abiTag='" + abiTag + '\''
            + ", platformTag='" + platformTag + '\''
            + '}';
    }

    static public Optional<PythonWheelDetails> fromFile(File file) {
        Pattern wheelPattern = Pattern.compile(WHEEL_FILE_FORMAT);
        Matcher matcher = wheelPattern.matcher(file.getName());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new PythonWheelDetails(file, matcher));
    }
}
