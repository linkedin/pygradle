package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.tasks.BuildWheelsTask;
import com.linkedin.gradle.python.tasks.ParallelWheelGenerationTask;
import com.linkedin.gradle.python.tasks.PipInstallTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;

public class WheelFirstPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();

        File cacheDir = new File(project.getGradle().getGradleUserHomeDir(), "pygradle-wheel-cache");

        project.getPlugins().withType(PythonPlugin.class, plugin -> {

            ParallelWheelGenerationTask parallelWheelTask = tasks.create("parallelWheels", ParallelWheelGenerationTask.class, it -> {
                ConfigurationContainer configurations = project.getConfigurations();
                FileCollection dependencies = configurations.getByName("python")
                    .plus(configurations.getByName("pygradleBootstrap"))
                    .plus(configurations.getByName("setupRequires"))
                    .plus(configurations.getByName("build"))
                    .plus(configurations.getByName("test"))
                    .plus(configurations.getByName("venv"));

                it.setFilesToConvert(dependencies);
                it.setCacheDir(cacheDir);
                it.dependsOn(tasks.getByName("createVirtualEnvironment"));
            });

            tasks.withType(PipInstallTask.class, it -> {
                it.dependsOn(parallelWheelTask);
                it.setWheelCache(cacheDir);
            });

            tasks.withType(BuildWheelsTask.class, it -> {
                it.dependsOn(parallelWheelTask);
                it.setWheelCacheDir(cacheDir);
            });

        });
    }
}
