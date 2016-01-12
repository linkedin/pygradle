package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.BasePythonRulePlugin;
import com.linkedin.gradle.python.plugin.internal.PythonRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonBaseLangPlugin implements Plugin<Project>  {

    public void apply(final Project project) {
        project.getPluginManager().apply(BasePythonRulePlugin.class);
        project.getPluginManager().apply(PythonRulePlugin.class);

        project.getExtensions().create("pythonConfigurations", PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
    }
}
