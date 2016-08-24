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

package com.linkedin.gradle.python.plugin.extension;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;


/**
 * Wrapper around a specific python configuration. It has some helper methods to get and set values as needed.
 */
public class PythonConfiguration {
    private final Configuration configuration;
    private final DependencyHandler dependencyHandler;

    public PythonConfiguration(Configuration configuration, DependencyHandler dependencyHandler) {
        this.configuration = configuration;
        this.dependencyHandler = dependencyHandler;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public FileCollection getAllArtifacts() {
        return getConfiguration();
    }

    public void addDependency(Object notation) {
        dependencyHandler.add(configuration.getName(), notation);
    }

    public void addArtifact(PublishArtifact artifact) {
        configuration.getArtifacts().add(artifact);
    }
}
