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
import com.linkedin.gradle.python.util.values.PyGradleConfiguration
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import static com.linkedin.gradle.python.util.values.PyGradleTask.*

class PythonWebApplicationPlugin extends AbstractPluginBase {

    public final static String GUNICORN_ENTRYPOINT = 'gunicorn.app.wsgiapp:run'

    @Override
    void applyTo(Project project) {

        addPluginLocal(PythonPexDistributionPlugin)

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
        addTaskLocal([name: BUILD_WEB_APPLICATION, type: BuildWebAppTask]) { task ->
            task.deployableExtension = deployableExtension
            task.wheelExtension = wheelExtension
            task.pexExtension = pexExtension
            task.pythonInterpreter = settings.details.systemPythonInterpreter.path
            task.executable = new File(deployableExtension.deployableBinDir, "gunicorn")
            task.entryPoint = GUNICORN_ENTRYPOINT
        }

        def packageDeployable = addTaskLocal([name: PACKAGE_WEB_APPLICATION, type: Tar]) {
            compression = Compression.GZIP
            baseName = project.name
            extension = 'tar.gz'
            from(deployableExtension.deployableBuildDir)
        }

        project.artifacts.add(PyGradleConfiguration.DEFAULT.value, packageDeployable)

        aDependsOnB(PACKAGE_WEB_APPLICATION, BUILD_WEB_APPLICATION)
        aDependsOnB(BUILD_WEB_APPLICATION, BUILD_PEX)
    }
}
