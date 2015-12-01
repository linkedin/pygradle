package com.linkedin.gradle.python.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonLangPlugin implements Plugin<Project>  {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonBaseLangPlugin.class);
    }
}
