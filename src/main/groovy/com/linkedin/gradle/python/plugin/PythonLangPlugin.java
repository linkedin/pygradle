package com.linkedin.gradle.python.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;
import org.gradle.nativeplatform.plugins.NativeComponentModelPlugin;

public class PythonLangPlugin implements Plugin<Project>  {

    public void apply(final Project project) {
        project.getPluginManager().apply(NativeComponentModelPlugin.class);
        project.getPluginManager().apply(ComponentModelBasePlugin.class);
        project.getPluginManager().apply(PythonRulePlugin.class);
    }
}
