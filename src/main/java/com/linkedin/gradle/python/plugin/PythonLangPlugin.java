package com.linkedin.gradle.python.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.internal.registry.ModelRegistry;

import javax.inject.Inject;


public class PythonLangPlugin implements Plugin<Project>  {

    private final ModelRegistry modelRegistry;

    @Inject
    public PythonLangPlugin(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonBaseLangPlugin.class);
    }
}
