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

import com.linkedin.gradle.python.extension.DeployableExtension
import com.linkedin.gradle.python.extension.PexExtension
import com.linkedin.gradle.python.extension.WheelExtension
import com.linkedin.gradle.python.tasks.BuildWebAppTask
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.StandardTextValues
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PythonWebApplicationPlugin extends PythonBasePlugin {

    public final static String TASK_BUILD_PEX = 'buildPex'
    public final static String TASK_BUILD_WEB_APPLICATION = 'buildWebApplication'
    public final static String TASK_PACKAGE_WEB_APPLICATION = 'packageWebApplication'

    public final static String GUNICORN_ENTRYPOINT = 'gunicorn.app.wsgiapp:run'

    @Override
    void applyTo(Project project) {

        project.plugins.apply(PythonPexDistributionPlugin)

        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(project)
        PexExtension pexExtension = ExtensionUtils.maybeCreatePexExtension(project)

        /**
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exploding gunicorn as a pex file.
         */
        project.tasks.create(TASK_BUILD_WEB_APPLICATION, BuildWebAppTask) { task ->
            task.description = 'Build a web app, by default using gunicorn, but it\'s configurable.'
            task.dependsOn(TASK_BUILD_PEX)
            task.deployableExtension = deployableExtension
            task.wheelExtension = wheelExtension
            task.pexExtension = pexExtension
            task.pythonInterpreter = settings.details.systemPythonInterpreter.path
            task.executable = new File(deployableExtension.deployableBinDir, "gunicorn")
            task.entryPoint = GUNICORN_ENTRYPOINT
        }

        def packageDeployable = project.tasks.create(TASK_PACKAGE_WEB_APPLICATION, Tar, new Action<Tar>() {
            @Override
            void execute(Tar tar) {
                tar.compression = Compression.GZIP
                tar.baseName = project.name
                tar.extension = 'tar.gz'
                tar.from(deployableExtension.deployableBuildDir)
            }
        })
        packageDeployable.dependsOn(project.tasks.getByName(TASK_BUILD_WEB_APPLICATION))

        project.artifacts.add(StandardTextValues.CONFIGURATION_DEFAULT.value, packageDeployable)

    }
}
