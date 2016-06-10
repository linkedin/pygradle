package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.tasks.GenerateCompletionsTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;


public class PythonCliDistributionPlugin extends PythonBasePlugin {

    public static final String TASK_GENERATE_COMPLETIONS = "generateCompletions";

    @Override
    public void applyTo(Project project) {
        project.getPlugins().apply(PythonPexDistributionPlugin.class);
        ExtensionUtils.maybeCreateCliExtension(project);

        GenerateCompletionsTask completionsTask = project.getTasks().create(TASK_GENERATE_COMPLETIONS, GenerateCompletionsTask.class);
        completionsTask.dependsOn(project.getTasks().getByName(PythonPlugin.TASK_INSTALL_PROJECT));

        project.getTasks().getByName(PythonPexDistributionPlugin.TASK_BUILD_PEX).dependsOn(project.getTasks().getByName(TASK_GENERATE_COMPLETIONS));
    }

}
