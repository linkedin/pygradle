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
package com.linkedin.gradle.python.wheel.internal;

import com.linkedin.gradle.python.wheel.AbiDetails;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import com.linkedin.gradle.python.wheel.PythonAbiContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultPythonAbiContainer implements EditablePythonAbiContainer, PythonAbiContainer, Serializable {

    private static final Logger logger = Logging.getLogger(PythonAbiContainer.class);

    private List<AbiDetails> supportedAbis = new ArrayList<>();

    @Override
    public void addSupportedAbi(AbiDetails triple) {
        supportedAbis.add(triple);
        logger.debug("Available ABI's: {}", supportedAbis);
    }

    @Override
    public boolean matchesSupportedVersion(File pythonExecutable, String pythonTag, String abiTag, String platformTag) {
        String[] pythonTags = pythonTag.split("\\.");
        String[] abiTags = abiTag.split("\\.");
        String[] platformTags = platformTag.split("\\.");

        return supportedAbis.stream()
            .filter(it -> Objects.equals(it.getPythonExecutable().getAbsolutePath(), pythonExecutable.getAbsolutePath()))
            .anyMatch(it -> contains(it, pythonTags, abiTags, platformTags));
    }

    @Override
    public EditablePythonAbiContainer copy() {
        DefaultPythonAbiContainer container = new DefaultPythonAbiContainer();
        container.supportedAbis = new ArrayList<>(supportedAbis);
        return container;
    }

    private static boolean contains(AbiDetails triple, String[] pythonTags, String[] abiTags, String[] platformTags) {
        return contains(triple.getPythonTag(), pythonTags)
            && contains(triple.getAbiTag(), abiTags)
            && contains(triple.getPlatformTag(), platformTags);
    }

    private static boolean contains(String needle, String[] haystack) {
        for (String s : haystack) {
            if (Objects.equals(needle, s)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "PythonAbiContainer{"
            + "supportedAbis=" + supportedAbis
            + '}';
    }
}
