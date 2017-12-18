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

import com.linkedin.gradle.python.extension.PlatformTag;
import com.linkedin.gradle.python.extension.PythonTag;
import com.linkedin.gradle.python.extension.PythonVersion;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WheelCache implements Serializable {

    private static final Logger logger = Logging.getLogger(WheelCache.class);

    private final File cacheDir;
    private final PythonTag pythonTag;
    private final PlatformTag platformTag;

    public WheelCache(File cacheDir, PythonTag pythonTag, PlatformTag platformTag) {
        this.cacheDir = cacheDir;
        this.pythonTag = pythonTag;
        this.platformTag = platformTag;
    }

    /**
     * Find's a wheel in the wheel cache.
     *
     * @param library name of the library
     * @param version version of the library
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

        Set<String> pythonAbi = new HashSet<>(Collections.singleton("py2.py3"));
        pythonAbi.add("py" + pythonVersion.getPythonMajor());
        pythonAbi.add("py" + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());

        List<String> platformTagList = new ArrayList<>(Arrays.asList("any.whl", platformTag.getPlatform() + ".whl"));

        logger.debug("Searching for {} {} with options: {}, {}", library, version, pythonAbi, platformTagList);

        List<File> collect = Arrays.stream(files)
            .filter(file -> pythonAbi.stream().anyMatch(it -> file.getName().contains(it)))
            .filter(file -> platformTagList.stream().anyMatch(it -> file.getName().endsWith(it)))
            .collect(Collectors.toList());

        logger.debug("Found artifacts: {}", collect);

        if (collect.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(collect.get(0));
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }
}
