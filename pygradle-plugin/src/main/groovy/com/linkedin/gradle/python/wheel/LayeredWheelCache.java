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
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;


public class LayeredWheelCache implements WheelCache, Serializable {

    private static final Logger logger = Logging.getLogger(LayeredWheelCache.class);

    private final Map<WheelCacheLayer, File> layeredCacheMap;
    private final PythonAbiContainer pythonAbiContainer;

    private boolean wheelsReady;

    public LayeredWheelCache(Map<WheelCacheLayer, File> layeredCacheMap, PythonAbiContainer pythonAbiContainer) {
        this.layeredCacheMap = layeredCacheMap;
        this.pythonAbiContainer = pythonAbiContainer;
        // Assume that we'll succeed building all wheels.
        wheelsReady = true;
    }

    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails) {
        for (WheelCacheLayer wheelCacheLayer : layeredCacheMap.keySet()) {
            Optional<File> wheel = findWheel(name, version, pythonDetails.getVirtualEnvInterpreter(), wheelCacheLayer);

            if (wheel.isPresent()) {
                return wheel;
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<File> findWheel(String name, String version, PythonDetails pythonDetails,
                                    WheelCacheLayer wheelCacheLayer) {
        return findWheel(name, version, pythonDetails.getVirtualEnvInterpreter(), wheelCacheLayer);
    }


    @Override
    public void storeWheel(File wheel) {
        for (WheelCacheLayer wheelCacheLayer : layeredCacheMap.keySet()) {
            storeWheel(wheel, wheelCacheLayer);
        }
    }

    @Override
    public void storeWheel(File wheel, WheelCacheLayer wheelCacheLayer) {
        File cacheDir = layeredCacheMap.get(wheelCacheLayer);

        if (wheel != null && cacheDir != null) {
            /*
             * Although it seemed unlikely, because we look for the wheel
             * before trying to store it, the following scenario is possible
             * and observed in testing.
             *
             * When two (or more) sub-projects do not find the wheel in any
             * layer of the cache during build, they all proceed with the
             * build into their respective project layers. One of them
             * finishes first and stores the wheel into the host layer,
             * if wheel build is not customized. The others then do not
             * need to store the same wheel.
             *
             * We do not try to overwrite the existing file, but instead
             * catch the exception and log it. The try-catch avoids race
             * conditions here much better than conditional expressions.
             * We re-throw other exceptions.
             *
             * The file attributes are preserved after copy.
             */
            try {
                Files.copy(wheel.toPath(), new File(cacheDir, wheel.getName()).toPath(), COPY_ATTRIBUTES);
            } catch (FileAlreadyExistsException e) {
                logger.info("Wheel {} already stored in {}", wheel.getName(), cacheDir.toString());
            } catch (NoSuchFileException e) {
                // Not really the end of the world if we cannot store from one layer into another.
                logger.info("Could not store the wheel {} into {}.", wheel.getName(), cacheDir.toString());
                if (!wheel.exists()) {
                    // Shouldn't happen. In our current use we always store after finding it in another layer.
                    logger.info("The wheel file does not exist");
                } else if (!cacheDir.exists()) {
                    /*
                     * This can happen when a rogue custom task from the project's Gradle
                     * settings either directly changes build directory or triggers a cleanup
                     * task by satisfying its conditions. Such custom tasks are usually injected
                     * without any proper scheduling and "dependsOn" use.
                     */
                    logger.info("The cache directory does not exist.");
                    if (cacheDir.mkdirs()) {
                        logger.info("It was successfully recreated.");
                    }
                }
                if (wheelCacheLayer == WheelCacheLayer.PROJECT_LAYER) {
                    // Let build wheels task know that we missed to prepare at least one wheel.
                    setWheelsReady(false);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public Optional<File> getTargetDirectory() {
        return Optional.ofNullable(layeredCacheMap.get(WheelCacheLayer.PROJECT_LAYER));
    }

    @Override
    public boolean isWheelsReady() {
        return wheelsReady;
    }

    @Override
    public void setWheelsReady(boolean wheelsReady) {
        this.wheelsReady = wheelsReady;
    }

    /**
     * Find a wheel from target layer.
     *
     * @param name package name
     * @param version package version
     * @param pythonExecutable Python interpreter executable file
     * @param wheelCacheLayer the {@link WheelCacheLayer} to fetch the wheel from
     * @return the wheel if found in the specified layer, otherwise {@code Optional.empty()}
     */
    private Optional<File> findWheel(String name, String version, File pythonExecutable,
                                    WheelCacheLayer wheelCacheLayer) {
        File cacheDir = layeredCacheMap.get(wheelCacheLayer);

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

    private boolean wheelMatches(File pythonExecutable, PythonWheelDetails wheelDetails) {
        return pythonAbiContainer.matchesSupportedVersion(
            pythonExecutable,
            wheelDetails.getPythonTag(),
            wheelDetails.getAbiTag(),
            wheelDetails.getPlatformTag());
    }
}
