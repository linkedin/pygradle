package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.sources.SourceDistRulePlugin;
import com.linkedin.gradle.python.plugin.internal.wheel.WheelRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonSourceDistPlugin implements Plugin<Project>  {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLangPlugin.class);
        project.getPluginManager().apply(SourceDistRulePlugin.class);
    }
}
