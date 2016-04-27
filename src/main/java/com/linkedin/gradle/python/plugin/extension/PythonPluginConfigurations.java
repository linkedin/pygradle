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
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;


/**
 * An extension that contains all of the python specific configurations. This is a bridge between the old modle and the
 * software model.
 */
public class PythonPluginConfigurations {

    public static final String BOOTSTRAP_CONFIGURATION = "bootstrap";
    public static final String VIRTUAL_ENV_CONFIGURATION = "virtualEnv";
    public static final String PYTHON_CONFIGURATION = "python";
    public static final String PYTEST_CONFIGURATION = "pytest";
    public static final String PYTHON_FLAKE8_CONFIGURATION = "flake8";

    final ConfigurationContainer configurations;
    final DependencyHandler dependencyHandler;

    private final PythonConfiguration virtualEnvPythonConfiguration;
    private final PythonConfiguration bootstrapPythonConfiguration;
    private final PythonConfiguration pythonPythonConfiguration;
    private final PythonConfiguration pythonTestPythonConfiguration;
    private final PythonConfiguration archivePythonConfiguration;

    public PythonPluginConfigurations(final ConfigurationContainer configurations, final DependencyHandler dependencyHandler) {
        this.configurations = configurations;
        this.dependencyHandler = dependencyHandler;

        Configuration virtualEnvConfiguration = configurations.create(VIRTUAL_ENV_CONFIGURATION);
        Configuration bootstrapConfiguration = configurations.create(BOOTSTRAP_CONFIGURATION);

        Configuration pythonConfiguration = configurations.create(PYTHON_CONFIGURATION);
        Configuration pyTestConfiguration = configurations.create(PYTEST_CONFIGURATION);
        pyTestConfiguration.extendsFrom(pythonConfiguration);

        configurations.create(PYTHON_FLAKE8_CONFIGURATION);

        this.virtualEnvPythonConfiguration = new PythonConfiguration(virtualEnvConfiguration, dependencyHandler);
        this.bootstrapPythonConfiguration = new PythonConfiguration(bootstrapConfiguration, dependencyHandler);
        this.pythonPythonConfiguration = new PythonConfiguration(pythonConfiguration, dependencyHandler);
        this.pythonTestPythonConfiguration = new PythonConfiguration(pyTestConfiguration, dependencyHandler);
        this.archivePythonConfiguration = new PythonConfiguration(configurations.findByName(Dependency.ARCHIVES_CONFIGURATION), dependencyHandler);

        bootstrapConfiguration.defaultDependencies(dependencies -> dependencies.add(dependencyHandler.create("pypi:virtualenv:15.0.1")));

        dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "pypi:pip:7.1.2");
        dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "pypi:setuptools-git:1.1");

        dependencyHandler.add(PYTHON_FLAKE8_CONFIGURATION, "pypi:flake8:2.4.0");
    }

    public PythonConfiguration getVirtualEnv() {
        return virtualEnvPythonConfiguration;
    }

    public PythonConfiguration getBootstrap() {
        return bootstrapPythonConfiguration;
    }

    public PythonConfiguration getPython() {
        return pythonPythonConfiguration;
    }

    public PythonConfiguration getPyTest() {
        return pythonTestPythonConfiguration;
    }

    public PythonConfiguration getArchive() {
        return archivePythonConfiguration;
    }

}
