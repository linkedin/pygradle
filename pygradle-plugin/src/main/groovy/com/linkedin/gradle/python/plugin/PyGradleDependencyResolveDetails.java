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
package com.linkedin.gradle.python.plugin;

import org.gradle.api.Action;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Map;
import java.util.Objects;


public class PyGradleDependencyResolveDetails implements Action<DependencyResolveDetails> {

    private static final Logger LOGGER = Logging.getLogger(PyGradleDependencyResolveDetails.class);

    private final Map<String, Map<String, String>> requiredVersions;

    public PyGradleDependencyResolveDetails(Map<String, Map<String, String>> requiredVersions) {
        this.requiredVersions = requiredVersions;
    }

    @Override
    public void execute(DependencyResolveDetails details) {
        if (requiredVersions.containsKey(details.getRequested().getName())) {
            String name = details.getRequested().getName();
            String version = requiredVersions.get(name).get("version");
            if (Objects.equals("", version) || null == version) {
                return;
            }
            LOGGER.info("Resolving {} to {}=={} per gradle-python resolution strategy.", name, name, version);
            details.useVersion(version);
        }
    }
}
