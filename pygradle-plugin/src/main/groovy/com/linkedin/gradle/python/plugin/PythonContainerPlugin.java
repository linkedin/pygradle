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
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.ZipappContainerExtension;
import com.linkedin.gradle.python.tasks.BuildWheelsTask;
import com.linkedin.gradle.python.tasks.NoopBuildPexTask;
import com.linkedin.gradle.python.tasks.NoopTask;
import com.linkedin.gradle.python.tasks.PythonContainerTask;
import com.linkedin.gradle.python.util.ApplicationContainer;
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

        ExtensionUtils.maybeCreate(project, "zipapp", ZipappContainerExtension.class);

        applicationContainer.addExtensions(project);

        /*
         * Build wheels, first of dependencies, then of the current project.
         * However, it's possible that we will have multiple containers
         * (e.g. pex and shiv), so be sure to only build the wheels once.
         *
         * TODO 2019-03-19: Adapt this to on-host layered caching.
         */
        TaskContainer tasks = project.getTasks();

        // Add this no-op task for backward compatibility.  See PexExtension for details.
        Task noop = tasks.findByName(PexExtension.TASK_BUILD_NOOP_PEX);
        if (noop == null) {
            noop = tasks.create(PexExtension.TASK_BUILD_NOOP_PEX, NoopBuildPexTask.class);
        }

        if (tasks.findByName(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS) == null) {
            BuildWheelsTask buildWheelsTask = tasks.create(ApplicationContainer.TASK_BUILD_WHEELS, BuildWheelsTask.class);
            buildWheelsTask.setInstallFileCollection(project.getConfigurations().getByName("python"));
            buildWheelsTask.dependsOn(tasks.getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue()));

            BuildWheelsTask projectWheelsTask = tasks.create(ApplicationContainer.TASK_BUILD_PROJECT_WHEEL, BuildWheelsTask.class);
            projectWheelsTask.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
            projectWheelsTask.setEnvironment(pythonExtension.pythonEnvironmentDistgradle);
            projectWheelsTask.dependsOn(tasks.getByName(ApplicationContainer.TASK_BUILD_WHEELS));

            /*
             * This is just a lifecycle task which provides a convenient place
             * to add specific container dependencies on, without those
             * extensions having to know too many intimate details about
             * generic Python builds.  E.g. we make the pex task depend on it.
             */
            Task assemble = tasks.create(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS);
            assemble.dependsOn(noop);

            Tar tar = tasks.create(ApplicationContainer.TASK_PACKAGE_DEPLOYABLE, Tar.class);
            tar.setCompression(Compression.GZIP);
            tar.setBaseName(project.getName());
            tar.setExtension("tar.gz");
            tar.from(deployableExtension.getDeployableBuildDir());
            tar.dependsOn(assemble);
            project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), tar);
        }

        // This must happen after build.gradle file evaluation.
        project.afterEvaluate(it -> {
            // The application container might have changed.
            final ApplicationContainer postContainer = pythonExtension.getApplicationContainer();

            if (postContainer == null) {
                throw new IllegalArgumentException(
                    "Unknown Python application container: "
                        + pythonExtension.getContainer());
            }

            /*
             * Plumb the container tasks into the task hierarchy.  The
             * assemble task depends on all the implementers of
             * PythonContainerTask, and the deployable task depends on the
             * assemble task.
             *
             * While we're doing this though, suppress the deprecation warning
             * normally thrown in NoopBuildPexTask when user code calls its
             * .dependsOn().
             */
            postContainer.addDependencies(project);
            postContainer.makeTasks(project);

            Task assemble = tasks.getByName(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS);
            Task parent = tasks.getByName(ApplicationContainer.TASK_BUILD_PROJECT_WHEEL);

            for (Task task : tasks.withType(PythonContainerTask.class)) {
                if (task instanceof NoopTask) {
                    ((NoopTask) task).setSuppressWarning(true);
                }

                assemble.dependsOn(task);
                task.dependsOn(parent);

                if (task instanceof NoopTask) {
                    ((NoopTask) task).setSuppressWarning(false);
                }
            }
        });
    }
}
