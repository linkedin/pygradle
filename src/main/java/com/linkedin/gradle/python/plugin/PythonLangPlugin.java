package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.plugin.internal.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.base.PythonBaseRulePlugin;
import com.linkedin.gradle.python.plugin.internal.base.PythonLanguageRulePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class PythonLangPlugin implements Plugin<Project>  {

    public static final String PYTHON_CONFIGURATIONS = "pythonConfigurations";

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLanguageRulePlugin.class);
        project.getPluginManager().apply(PythonBaseRulePlugin.class);

        if (project.getExtensions().findByName(PYTHON_CONFIGURATIONS) == null) {
            project.getExtensions().create(PYTHON_CONFIGURATIONS, PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());
        }
    }
}
