package com.linkedin.gradle.python.plugin;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;


public class PythonPluginConfigurations {

  public static final String BOOTSTRAP_CONFIGURATION = "bootstrap";
  public static final String VIRTUAL_ENV_CONFIGURATION = "virtualEnv";
  public static final String PYTHON_CONFIGURATION = "python";
  public static final String PYTEST_CONFIGURATION = "pytest";

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
    dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "pypi:flake8:2.4.0");
//    dependencyHandler.add(VIRTUAL_ENV_CONFIGURATION, "com.linkedin.pygradle-setuptools:distgradle:0.2.10");


    Configuration pythonConfiguration = configurations.create(PYTHON_CONFIGURATION);
    Configuration pyTestConfiguration = configurations.create(PYTEST_CONFIGURATION);
    pyTestConfiguration.extendsFrom(pythonConfiguration);
  }

  public PythonConfiguration getVirtualEnv() {
    return new PythonConfiguration(VIRTUAL_ENV_CONFIGURATION);
  }

  public PythonConfiguration getBootstrap() {
    return new PythonConfiguration(BOOTSTRAP_CONFIGURATION);
  }

  public PythonConfiguration getPython() {
    return new PythonConfiguration(PYTHON_CONFIGURATION);
  }

  public PythonConfiguration getPyTest() {
    return new PythonConfiguration(PYTEST_CONFIGURATION);
  }

  class PythonConfiguration {
    private final String name;

    PythonConfiguration(String name) {
      this.name = name;
    }

    Configuration getConfiguration() {
      return configurations.getByName(name);
    }

    FileCollection getAllArtifacts() {
      return getConfiguration();
    }

    void addDependency(Object notation) {
      dependencyHandler.add(name, notation);
    }

    void addArtifact(PublishArtifact artifact) {
      configurations.getByName(name).getArtifacts().add(artifact);
    }
  }

}
