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
    private static final String WHEEL_FILE_FORMAT = "(?<dist>.+?)-(?<version>\\d.*?)(-(\\d.*?))?-(?<pythonTag>.+?)-(?<abiTag>.+?)-(?<platformTag>.+?).whl";

    private static final Pattern WHEEL_PATTERN = Pattern.compile(WHEEL_FILE_FORMAT);
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("_(?<snapshot>[A-Z]+)$");

    private final File file;
    private final String dist;
    private final String version;
    private final String pythonTag;
    private final String abiTag;
    private final String platformTag;

    private PythonWheelDetails(File wheelFile, Matcher matcher) {
        file = wheelFile;
        dist = matcher.group("dist");
        String matchedVersion = matcher.group("version");
        pythonTag = matcher.group("pythonTag");
        abiTag = matcher.group("abiTag");
        platformTag = matcher.group("platformTag");

        Matcher snapshotMatcher = SNAPSHOT_PATTERN.matcher(matchedVersion);
        if (snapshotMatcher.find()) {
            version = snapshotMatcher.replaceFirst("-" + snapshotMatcher.group("snapshot"));
        } else {
            version = matchedVersion;
        }
    }

    public File getFile() {
        return file;
    }

    public String getDist() {
        return dist;
    }

    public String getVersion() {
        return version;
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

    public static Optional<PythonWheelDetails> fromFile(File file) {
        Matcher matcher = WHEEL_PATTERN.matcher(file.getName());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new PythonWheelDetails(file, matcher));
    }
}
