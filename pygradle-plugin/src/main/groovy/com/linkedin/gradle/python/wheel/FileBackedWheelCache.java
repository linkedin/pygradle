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

import com.linkedin.gradle.python.extension.PythonDetails;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileBackedWheelCache implements WheelCache, Serializable {

    private static final Logger logger = Logging.getLogger(FileBackedWheelCache.class);

    // PEP-0427
    private static final String WHEEL_FILE_FORMAT = "(?<dist>.*?)-(?<version>.*?)(-(.*?))?-(?<pythonTag>.*?)-(?<abiTag>.*?)-(?<platformTag>.*?).whl";

    private final File cacheDir;
    private final Pattern wheelPattern;
    private final SupportedWheelFormats supportedWheelFormats;
    private final List<Function<String, Boolean>> versionFilter;
    private final List<Function<String, Boolean>> dependencyFilter;

    public FileBackedWheelCache(File cacheDir, SupportedWheelFormats supportedWheelFormats) {
        this.cacheDir = cacheDir;
        this.supportedWheelFormats = supportedWheelFormats;
        this.wheelPattern = Pattern.compile(WHEEL_FILE_FORMAT);
        this.versionFilter = new ArrayList<>();
        this.dependencyFilter = new ArrayList<>();
    }

    @Override
    public void addVersionFilter(Function<String, Boolean> filter) {
        versionFilter.add(filter);
    }

    @Override
    public boolean isWheelForVersionCacheable(String version) {
        return versionFilter.stream().anyMatch(it -> it.apply(version));
    }

    @Override
    public void addDependencyFilter(Function<String, Boolean> filter) {
        dependencyFilter.add(filter);
    }

    @Override
    public boolean isWheelForDependencyCacheable(String dependencyName) {
        return dependencyFilter.stream().anyMatch(it -> it.apply(dependencyName));
    }

    /**
     * Find's a wheel in the wheel cache.
     *
     * @param library       name of the library
     * @param version       version of the library
     * @param pythonDetails details on the python to find a wheel for
     * @return A wheel that could be used in it's place. If not found, {@code Optional.empty()}
     */
    @Override
    public Optional<File> findWheel(String library, String version, PythonDetails pythonDetails) {
        return findWheel(library, version, pythonDetails.getVirtualEnvInterpreter());
    }

    /**
     * Find's a wheel in the wheel cache.
     *
     * @param library          name of the library
     * @param version          version of the library
     * @param pythonExecutable Python Executable
     * @return A wheel that could be used in it's place. If not found, {@code Optional.empty()}
     */
    public Optional<File> findWheel(String library, String version, File pythonExecutable) {
        if (cacheDir == null) {
            return Optional.empty();
        }

        if (version == null) {
            return Optional.empty();
        }

        if (isWheelForVersionCacheable(version) || isWheelForDependencyCacheable(library)) {
            return Optional.empty();
        }

        String wheelPrefix = library.replace("-", "_") + "-" + version;
        logger.info("Searching for {} {} with prefix {}", library, version, wheelPrefix);
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(wheelPrefix) && name.endsWith(".whl"));

        if (files == null) {
            return Optional.empty();
        }

        List<PythonWheelDetails> wheelDetails = Arrays.stream(files).map(this::fromFile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        logger.info("Wheels for version of library: {}", wheelDetails);

        Optional<PythonWheelDetails> foundWheel = wheelDetails.stream()
            .filter(it -> wheelMatches(pythonExecutable, it))
            .findFirst();

        logger.info("Found artifacts: {}", foundWheel);

        return foundWheel.map(it -> it.file);
    }

    public File getCacheDir() {
        return cacheDir;
    }

    private boolean wheelMatches(File pythonExecutable, PythonWheelDetails wheelDetails) {
        return supportedWheelFormats.matchesSupportedVersion(
            pythonExecutable,
            wheelDetails.pythonTag,
            wheelDetails.abiTag,
            wheelDetails.platformTag);
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
