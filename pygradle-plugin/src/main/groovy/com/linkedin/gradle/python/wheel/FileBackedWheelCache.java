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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileBackedWheelCache implements WheelCache, Serializable {

    private static final Logger logger = Logging.getLogger(FileBackedWheelCache.class);

    private final File cacheDir;
    private final PythonAbiContainer pythonAbiContainer;

    public FileBackedWheelCache(File cacheDir, PythonAbiContainer pythonAbiContainer) {
        this.cacheDir = cacheDir;
        this.pythonAbiContainer = pythonAbiContainer;
    }

    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails) {
        return findWheel(name, version, pythonDetails.getVirtualEnvInterpreter());
    }

    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails,
                                    WheelCacheLayer wheelCacheLayer) {
        return Optional.empty();
    }

    @Override
    public void storeWheel(File wheel) { }

    @Override
    public void storeWheel(File wheel, WheelCacheLayer wheelCacheLayer) { }

    @Override
    public Optional<File> getTargetDirectory() {
        return Optional.empty();
    }

    @Override
    public boolean isWheelsReady() {
        return false;
    }

    @Override
    public void setWheelsReady(boolean wheelsReady) { }

    /**
     * Finds a wheel in the cache.
     *
     * @param name package name
     * @param version package version
     * @param pythonExecutable Python interpreter executable file
     * @return the wheel if found in the cache, otherwise {@code Optional.empty()}
     */
    public Optional<File> findWheel(String name, String version, File pythonExecutable) {
        if (cacheDir == null) {
            return Optional.empty();
        }

        /*
         * NOTE: Careful here! The prefix *must* end with a hyphen.
         * Otherwise 0.0.2 version will match 0.0.20.
         * Both name and version of the package must replace hyphen with underscore.
         * See PEP 427: https://www.python.org/dev/peps/pep-0427/
         */
        String wheelPrefix = (
                name.replace("-", "_")
                + "-"
                + version.replace("-", "_")
                + "-"
        );
        logger.info("Searching for {} {} with prefix {}", name, version, wheelPrefix);
        File[] files = cacheDir.listFiles((dir, entry) -> entry.startsWith(wheelPrefix) && entry.endsWith(".whl"));

        if (files == null) {
            return Optional.empty();
        }

        List<PythonWheelDetails> wheelDetails = Arrays.stream(files).map(PythonWheelDetails::fromFile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        logger.info("Wheels for version of package: {}", wheelDetails);

        Optional<PythonWheelDetails> foundWheel = wheelDetails.stream()
            .filter(it -> wheelMatches(pythonExecutable, it))
            .findFirst();

        logger.info("Found artifacts: {}", foundWheel);

        return foundWheel.map(it -> it.getFile());
    }

    public File getCacheDir() {
        return cacheDir;
    }

    private boolean wheelMatches(File pythonExecutable, PythonWheelDetails wheelDetails) {
        return pythonAbiContainer.matchesSupportedVersion(
            pythonExecutable,
            wheelDetails.getPythonTag(),
            wheelDetails.getAbiTag(),
            wheelDetails.getPlatformTag());
    }
}
