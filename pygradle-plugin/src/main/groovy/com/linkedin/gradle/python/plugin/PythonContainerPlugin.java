/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.ContainerExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.tasks.BuildWheelsTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;


public class PythonContainerPlugin extends PythonBasePlugin {
    @Override
    public void applyTo(final Project project) {

        project.getPlugins().apply(PythonPlugin.class);
        final PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        ExtensionUtils.maybeCreateWheelExtension(project);

        final DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project);
        final ContainerExtension containerExtension = pythonExtension.getContainerExtension();

        containerExtension.prepareExtension(project);

        /*
         * Build wheels, first of dependencies, then of the current project.
         * However, it's possible that we will have multiple containers
         * (e.g. pex and shiv), so be sure to only build the wheels once.
         */
        TaskContainer tasks = project.getTasks();

        if (tasks.withType(BuildWheelsTask.class).size() == 0) {
            BuildWheelsTask buildWheelsTask = tasks.create(ContainerExtension.TASK_BUILD_WHEELS, BuildWheelsTask.class);
            buildWheelsTask.dependsOn(tasks.getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue()));
            buildWheelsTask.setInstallFileCollection(project.getConfigurations().getByName("python"));

            BuildWheelsTask projectWheelsTask = tasks.create(
                ContainerExtension.TASK_BUILD_PROJECT_WHEEL, BuildWheelsTask.class, task -> {
                    task.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
                    task.setEnvironment(pythonExtension.pythonEnvironmentDistgradle);
                });
            projectWheelsTask.dependsOn(tasks.getByName(ContainerExtension.TASK_BUILD_WHEELS));
        }

        containerExtension.addTasks(project);

        Tar tar = tasks.create(ContainerExtension.TASK_PACKAGE_DEPLOYABLE, Tar.class);
        tar.setCompression(Compression.GZIP);
        tar.setBaseName(project.getName());
        tar.setExtension("tar.gz");
        tar.from(deployableExtension.getDeployableBuildDir());

        tar.dependsOn(tasks.getByName(ContainerExtension.TASK_BUILD_CONTAINER));

        project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), tar);
    }
}
