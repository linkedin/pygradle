package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.base.PythonLanguageRulePlugin;
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonBaseLangPlugin implements Plugin<Project>  {

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLanguageRulePlugin.class);
        project.getPluginManager().apply(PythonBaseRulePlugin.class);

        project.getExtensions().create("pythonConfigurations", PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
    }
}
