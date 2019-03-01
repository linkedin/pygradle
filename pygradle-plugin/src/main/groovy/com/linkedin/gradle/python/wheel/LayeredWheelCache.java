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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;


public class LayeredWheelCache implements WheelCache, Serializable {

    private static final Logger logger = Logging.getLogger(LayeredWheelCache.class);

    private final Map<WheelCacheLayer, File> layeredCacheMap;
    private final PythonAbiContainer pythonAbiContainer;

    public LayeredWheelCache(Map<WheelCacheLayer, File> layeredCacheMap, PythonAbiContainer pythonAbiContainer) {
        this.layeredCacheMap = layeredCacheMap;
        this.pythonAbiContainer = pythonAbiContainer;
    }

    /**
     * Find a wheel from all wheel cache layers.
     *
     * @param library       name of the library
     * @param version       version of the library
     * @param pythonDetails details on the python to find a wheel for
     * @return A wheel that could be used in it's place. If not found, {@code Optional.empty()}
     */
    @Override
    public Optional<File> findWheel(String library, String version, PythonDetails pythonDetails) {
        // TODO: Make sure layeredCacheMap is a LinkedHashMap when we initialize it in the plugin.
        for (WheelCacheLayer wheelCacheLayer : layeredCacheMap.keySet()) {
            Optional<File> layerResult = findWheelInLayer(library, version, pythonDetails.getVirtualEnvInterpreter(), wheelCacheLayer);

            if (layerResult.isPresent()) {
                return layerResult;
            }
        }

        return Optional.empty();
    }

    /**
     * Find wheel based on cache layer.
     *
     * @param library         name of the library
     * @param version         version of the library
     * @param pythonDetails   details on the python to find a wheel for
     * @param wheelCacheLayer which {@link WheelCacheLayer} to fetch wheel
     * @return a wheel that could be used in the target layer. If not found, {@code Optional.empty()}
     */
    @Override
    public Optional<File> findWheel(String library, String version, PythonDetails pythonDetails, WheelCacheLayer wheelCacheLayer) {
        return findWheelInLayer(library, version, pythonDetails.getVirtualEnvInterpreter(), wheelCacheLayer);
    }

    /**
     * Find a wheel from target layer.
     *
     * @param library          name of the library
     * @param version          version of the library
     * @param pythonExecutable python executable
     * @param wheelCacheLayer  which {@link WheelCacheLayer} to fetch wheel
     * @return A wheel that could be used in it's place. If not found, {@code Optional.empty()}
     */
    public Optional<File> findWheelInLayer(String library, String version, File pythonExecutable, WheelCacheLayer wheelCacheLayer) {
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
                library.replace("-", "_")
                + "-"
                + version.replace("-", "_")
                + "-"
        );
        logger.info("Searching for {} {} with prefix {}", library, version, wheelPrefix);
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(wheelPrefix) && name.endsWith(".whl"));

        if (files == null) {
            return Optional.empty();
        }

        List<PythonWheelDetails> wheelDetails = Arrays.stream(files).map(PythonWheelDetails::fromFile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        logger.info("Wheels for version of library: {}", wheelDetails);

        Optional<PythonWheelDetails> foundWheel = wheelDetails.stream()
            .filter(it -> wheelMatches(pythonExecutable, it))
            .findFirst();

        logger.info("Found artifacts: {}", foundWheel);

        return foundWheel.map(it -> it.getFile());
    }

    /**
     * Store given wheel file to target layer.
     *
     * @param wheelFile       the wheel file to store
     * @param wheelCacheLayer which {@link WheelCacheLayer} to store wheel
     */
    @Override
    public void storeWheel(File wheelFile, WheelCacheLayer wheelCacheLayer) {
        File cacheDir = layeredCacheMap.get(wheelCacheLayer);

        if (wheelFile != null && cacheDir != null) {
            try {
                Files.copy(wheelFile.toPath(), new File(cacheDir, wheelFile.getName()).toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private boolean wheelMatches(File pythonExecutable, PythonWheelDetails wheelDetails) {
        return pythonAbiContainer.matchesSupportedVersion(
            pythonExecutable,
            wheelDetails.getPythonTag(),
            wheelDetails.getAbiTag(),
            wheelDetails.getPlatformTag());
    }
}
