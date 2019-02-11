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
import com.linkedin.gradle.python.util.ApplicationContainer;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PythonContainerTask;
import com.linkedin.gradle.python.tasks.BuildWheelsTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;
import org.gradle.api.Task;
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
        final ApplicationContainer applicationContainer = pythonExtension.getApplicationContainer();

        // in an afterEvaluate?
        applicationContainer.prepareExtension(project);

        /*
         * Build wheels, first of dependencies, then of the current project.
         * However, it's possible that we will have multiple containers
         * (e.g. pex and shiv), so be sure to only build the wheels once.
         */
        TaskContainer tasks = project.getTasks();

        if (tasks.findByName(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS) == null) {
            BuildWheelsTask buildWheelsTask = tasks.create(ApplicationContainer.TASK_BUILD_WHEELS, BuildWheelsTask.class);
            buildWheelsTask.setInstallFileCollection(project.getConfigurations().getByName("python"));
            buildWheelsTask.dependsOn(tasks.getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue()));

            BuildWheelsTask projectWheelsTask = tasks.create(ApplicationContainer.TASK_BUILD_PROJECT_WHEEL, BuildWheelsTask.class);
            projectWheelsTask.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
            projectWheelsTask.setEnvironment(pythonExtension.pythonEnvironmentDistgradle);
            projectWheelsTask.dependsOn(tasks.getByName(ApplicationContainer.TASK_BUILD_WHEELS));

            /* This is just a lifecycle task which provides a convenient place
             * to add specific container dependencies on, without those
             * extensions having to know too many intimate details about
             * generic Python builds.  E.g. we make the pex task depend on it.
             */
            Task assemble = tasks.create(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS);

            Tar tar = tasks.create(ApplicationContainer.TASK_PACKAGE_DEPLOYABLE, Tar.class);
            tar.setCompression(Compression.GZIP);
            tar.setBaseName(project.getName());
            tar.setExtension("tar.gz");
            tar.from(deployableExtension.getDeployableBuildDir());
            tar.dependsOn(assemble);
            project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), tar);
        }

        // in an afterEvaluate?
        applicationContainer.makeTasks(project);

        Task assemble = tasks.getByName(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS);
        Task parent = tasks.getByName(ApplicationContainer.TASK_BUILD_PROJECT_WHEEL);

        for (Task task : tasks.withType(PythonContainerTask.class)) {
            assemble.dependsOn(task);
            task.dependsOn(parent);
        }
    }
}
