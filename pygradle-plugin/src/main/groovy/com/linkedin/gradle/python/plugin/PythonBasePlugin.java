package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class PythonBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        this.project = target;
        target.getPlugins().apply(PythonPlugin.class);
        settings = project.getExtensions().getByType(PythonExtension.class);
        applyTo(target);
    }

    public abstract void applyTo(Project project);

    public PythonExtension settings;
    public Project project;
}
