package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.tasks.GenerateCompletionsTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;


public class PythonCliDistributionPlugin extends PythonBasePlugin {

    public static final String TASK_GENERATE_COMPLETIONS = "generateCompletions";

    @Override
    public void applyTo(Project project) {
        project.getPlugins().apply(PythonPexDistributionPlugin.class);
        CliExtension cli = ((ExtensionAware) settings).getExtensions().create("cli", CliExtension.class);

        GenerateCompletionsTask completionsTask = project.getTasks().create(TASK_GENERATE_COMPLETIONS, GenerateCompletionsTask.class);
        completionsTask.dependsOn(project.getTasks().getByName(PythonPlugin.TASK_INSTALL_PROJECT));

        project.getTasks().getByName(PythonPexDistributionPlugin.TASK_BUILD_PEX).dependsOn(project.getTasks().getByName(TASK_GENERATE_COMPLETIONS));
    }

}
