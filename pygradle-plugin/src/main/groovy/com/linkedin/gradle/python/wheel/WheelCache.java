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

import com.linkedin.gradle.python.extension.PlatformTag;
import com.linkedin.gradle.python.extension.PythonTag;
import com.linkedin.gradle.python.extension.PythonVersion;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WheelCache implements Serializable {

    private static final Logger logger = Logging.getLogger(WheelCache.class);

    // PEP-0427
    private static final String WHEEL_FILE_FORMAT = "(?<dist>.*?)-(?<version>.*?)(-(.*?))?-(?<pythonTag>.*?)-(?<abiTag>.*?)-(?<platformTag>.*?).whl";

    private final File cacheDir;
    private final PythonTag pythonTag;
    private final PlatformTag platformTag;
    private final Pattern wheelPattern;

    public WheelCache(File cacheDir, PythonTag pythonTag, PlatformTag platformTag) {
        this.cacheDir = cacheDir;
        this.pythonTag = pythonTag;
        this.platformTag = platformTag;
        this.wheelPattern = Pattern.compile(WHEEL_FILE_FORMAT);
    }

    /**
     * Find's a wheel in the wheel cache.
     *
     * @param library       name of the library
     * @param version       version of the library
     * @param pythonVersion version of python to search for
     * @return A wheel that could be used in it's place. If not found, {@code Optional.empty()}
     */
    public Optional<File> findWheel(String library, String version, PythonVersion pythonVersion) {

        if (cacheDir == null) {
            return Optional.empty();
        }

        String wheelPrefix = library.replace("-", "_") + "-" + version;
        logger.debug("Searching for {} {} with prefix {}", library, version, wheelPrefix);
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(wheelPrefix) && name.endsWith(".whl"));

        if (files == null) {
            return Optional.empty();
        }

        List<PythonWheelDetails> wheelDetails = Arrays.stream(files).map(this::fromFile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        logger.debug("Wheels for version of library: {}", wheelDetails);

        Set<String> pythonAbi = new HashSet<>(Collections.singleton("py2.py3"));
        pythonAbi.add("py" + pythonVersion.getPythonMajor());
        pythonAbi.add("py" + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());

        Set<String> platformTagList = new HashSet<>(Arrays.asList("any", platformTag.getPlatform()));

        logger.debug("Searching for {} {} with options: {}, {}", library, version, pythonAbi, platformTagList);

        Optional<PythonWheelDetails> foundWheel = wheelDetails.stream()
            .filter(details -> wheelMatches(details, pythonAbi, platformTagList)).findFirst();

        logger.debug("Found artifacts: {}", foundWheel);

        return foundWheel.map(it -> it.file);
    }

    public File getCacheDir() {
        return cacheDir;
    }

    private boolean wheelMatches(PythonWheelDetails wheelDetails, Set<String> acceptablePythonTeg, Set<String> acceptablePlatformTag) {
        return acceptablePythonTeg.contains(wheelDetails.pythonTag) && acceptablePlatformTag.contains(wheelDetails.platformTag);
    }

    private Optional<PythonWheelDetails> fromFile(File file) {
        Matcher matcher = wheelPattern.matcher(file.getName());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(new PythonWheelDetails(file, matcher));
    }

    private class PythonWheelDetails {

        final private File file;
        final private String dist;
        final private String version;
        final private String pythonTag;
        final private String abiTag;
        final private String platformTag;

        private PythonWheelDetails(File file, Matcher matcher) {
            this.file = file;
            this.dist = matcher.group("dist");
            this.version = matcher.group("version");
            this.pythonTag = matcher.group("pythonTag");
            this.abiTag = matcher.group("abiTag");
            this.platformTag = matcher.group("platformTag");
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
    }
}
