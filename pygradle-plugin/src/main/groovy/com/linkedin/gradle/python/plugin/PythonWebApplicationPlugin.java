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

import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.tasks.BuildWebAppTask;
import com.linkedin.gradle.python.util.ApplicationContainer;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;

import java.io.File;

public class PythonWebApplicationPlugin extends PythonBasePlugin {

    public static final String TASK_BUILD_WEB_APPLICATION = "buildWebApplication";
    public static final String TASK_PACKAGE_WEB_APPLICATION = "packageWebApplication";
    public static final String GUNICORN_ENTRYPOINT = "gunicorn.app.wsgiapp:run";
    public static final String GUNICORN = "gunicorn";


    @Override
    public void applyTo(final Project project) {
        project.getPlugins().apply(PythonContainerPlugin.class);

        final DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project);

        // 2019-04-11(warsaw): FIXME: For now, we're still hard coding pex
        // for the gunicorn file.  Make sure the `pex` dependency is
        // installed.
        new PexExtension(project).addDependencies(project);

        /*
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exposing gunicorn as a pex file.
         */
        project.getTasks().create(TASK_BUILD_WEB_APPLICATION, BuildWebAppTask.class, task -> {
            task.setDescription("Build a web app, by default using gunicorn, but it's configurable.");
            task.mustRunAfter(ApplicationContainer.TASK_ASSEMBLE_CONTAINERS);
            task.setExecutable(new File(deployableExtension.getDeployableBinDir(), GUNICORN));
            task.setEntryPoint(GUNICORN_ENTRYPOINT);
        });

        // Make packaging task wait on this task so that gunicorn is packed into the app.
        project.getTasks().getByName(ApplicationContainer.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.getTasks().getByName(TASK_BUILD_WEB_APPLICATION));

        /*
         * TODO: Remove this task once the backwards compatibility is not needed for it any more.
         */
        project.getTasks().create(TASK_PACKAGE_WEB_APPLICATION, task -> {
            task.setDescription("Backward compatibility place-holder task for packaging web app");
            task.dependsOn(project.getTasks().getByName(TASK_BUILD_WEB_APPLICATION));
            task.setEnabled(false);
        });

        project.getTasks().getByName(ApplicationContainer.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.getTasks().getByName(TASK_PACKAGE_WEB_APPLICATION));
    }
}
