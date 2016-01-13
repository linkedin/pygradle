package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.language.PythonLangRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonLangPlugin implements Plugin<Project>  {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonBaseLangPlugin.class);
        project.getPluginManager().apply(PythonLangRulePlugin.class);
    }
}
