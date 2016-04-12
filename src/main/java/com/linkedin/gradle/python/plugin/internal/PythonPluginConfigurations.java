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

package com.linkedin.gradle.python.plugin.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;


public class PythonPluginConfigurations {

    public static final String BOOTSTRAP_CONFIGURATION = "bootstrap";
    public static final String VIRTUAL_ENV_CONFIGURATION = "virtualEnv";
    public static final String PYTHON_CONFIGURATION = "python";
    public static final String PYTEST_CONFIGURATION = "pytest";
    public static final String PYTHON_FLAKE8_CONFIGURATION = "flake8";

    private final ConfigurationContainer configurations;
    private final DependencyHandler dependencyHandler;

    public PythonPluginConfigurations(final ConfigurationContainer configurations, final DependencyHandler dependencyHandler) {
        this.configurations = configurations;
        this.dependencyHandler = dependencyHandler;

        Configuration bootstrapConfiguration = configurations.create(BOOTSTRAP_CONFIGURATION);
        bootstrapConfiguration.defaultDependencies(new Action<DependencySet>() {
            @Override
            public void execute(DependencySet dependencies) {
                dependencies.add(dependencyHandler.create("pypi:virtualenv:13.1.2"));
            }
        });

        configurations.create(VIRTUAL_ENV_CONFIGURATION);
        dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "pypi:pip:7.1.2");
        dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "pypi:setuptools-git:1.1");

        Configuration pythonConfiguration = configurations.create(PYTHON_CONFIGURATION);
        Configuration pyTestConfiguration = configurations.create(PYTEST_CONFIGURATION);
        pyTestConfiguration.extendsFrom(pythonConfiguration);

        configurations.create(PYTHON_FLAKE8_CONFIGURATION);
        dependencyHandler.add(PYTHON_FLAKE8_CONFIGURATION, "pypi:flake8:2.4.0");
    }

    public PythonConfiguration getVirtualEnv() {
        return new PythonConfiguration(VIRTUAL_ENV_CONFIGURATION);
    }

    public PythonConfiguration getBootstrap() {
        return new PythonConfiguration(BOOTSTRAP_CONFIGURATION);
    }

    public PythonConfiguration getPythonDocs() {
        return new PythonConfiguration(PYTHON_FLAKE8_CONFIGURATION);
    }

    public PythonConfiguration getPython() {
        return new PythonConfiguration(PYTHON_CONFIGURATION);
    }

    public PythonConfiguration getPyTest() {
        return new PythonConfiguration(PYTEST_CONFIGURATION);
    }

    public PythonConfiguration getArchive() {
        return new PythonConfiguration(Dependency.ARCHIVES_CONFIGURATION);
    }

    public class PythonConfiguration {
        private final String name;

        PythonConfiguration(String name) {
            this.name = name;
        }

        public Configuration getConfiguration() {
            return configurations.getByName(name);
        }

        public FileCollection getAllArtifacts() {
            return getConfiguration();
        }

        public void addDependency(Object notation) {
            dependencyHandler.add(name, notation);
        }

        public void addArtifact(PublishArtifact artifact) {
            configurations.getByName(name).getArtifacts().add(artifact);
        }
    }

}
