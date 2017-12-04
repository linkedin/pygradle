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
package com.linkedin.gradle.python.plugin

import org.gradle.api.Project
import org.gradle.api.Task

import com.linkedin.gradle.python.extension.DeployableExtension
import com.linkedin.gradle.python.tasks.BuildWebAppTask
import com.linkedin.gradle.python.util.ExtensionUtils


class PythonWebApplicationPlugin extends PythonBasePlugin {

    public final static String TASK_BUILD_PEX = 'buildPex'
    public final static String TASK_BUILD_WEB_APPLICATION = 'buildWebApplication'
    public final static String TASK_PACKAGE_WEB_APPLICATION = 'packageWebApplication'

    public final static String GUNICORN_ENTRYPOINT = 'gunicorn.app.wsgiapp:run'

    @Override
    void applyTo(Project project) {

        project.plugins.apply(PythonPexDistributionPlugin)

        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)

        /*
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exposing gunicorn as a pex file.
         */
        project.tasks.create(TASK_BUILD_WEB_APPLICATION, BuildWebAppTask) { task ->
            task.description = "Build a web app, by default using gunicorn, but it's configurable."
            task.dependsOn(TASK_BUILD_PEX)
            task.executable = new File(deployableExtension.deployableBinDir, "gunicorn")
            task.entryPoint = GUNICORN_ENTRYPOINT
        }

        // Make packaging task wait on this task so that gunicorn is packed into the app.
        project.tasks.getByName(PythonPexDistributionPlugin.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.tasks.getByName(TASK_BUILD_WEB_APPLICATION))

        /*
         * TODO: Remove this task once the backwards compatibility is not needed for it any more.
         */
        project.tasks.create(TASK_PACKAGE_WEB_APPLICATION) { Task task ->
            task.description = "Backward compatibility place-holder task for packaging web app"
            task.dependsOn(project.tasks.getByName(TASK_BUILD_WEB_APPLICATION))
            task.enabled = false
        }
        project.tasks.getByName(PythonPexDistributionPlugin.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.tasks.getByName(TASK_PACKAGE_WEB_APPLICATION))
    }
}
