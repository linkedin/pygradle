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
import org.gradle.api.file.FileCollection;


/**
 * Wrapper around a specific python configuration. It has some helper methods to get and set values as needed.
 */
public class PythonConfiguration {
    private PythonPluginConfigurations pythonPluginConfigurations;
    private final String name;

    PythonConfiguration(PythonPluginConfigurations pythonPluginConfigurations, String name) {
        this.pythonPluginConfigurations = pythonPluginConfigurations;
        this.name = name;
    }

    public Configuration getConfiguration() {
        return pythonPluginConfigurations.configurations.getByName(name);
    }

    public FileCollection getAllArtifacts() {
        return getConfiguration();
    }

    public void addDependency(Object notation) {
        pythonPluginConfigurations.dependencyHandler.add(name, notation);
    }

    public void addArtifact(PublishArtifact artifact) {
        pythonPluginConfigurations.configurations.getByName(name).getArtifacts().add(artifact);
    }
}
