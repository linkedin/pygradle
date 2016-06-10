package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.PythonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class PythonBasePlugin extends PythonHelpers implements Plugin<Project> {

  public PythonExtension settings
  public Project project

  @Override
  void apply(Project target) {
    this.project = target;
    target.plugins.apply(PythonPlugin)
    settings = project.getExtensions().getByType(PythonExtension)
    applyTo(target)
  }

  abstract void applyTo(Project project);
}
