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
import com.linkedin.gradle.python.tasks.BuildPexTask
import com.linkedin.gradle.python.tasks.BuildWheelsTask
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.StandardTextValues
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PythonPexDistributionPlugin extends PythonBasePlugin {

    public final static String TASK_BUILD_WHEELS = 'buildWheels'
    public final static String TASK_BUILD_PEX = 'buildPex'
    public final static String TASK_PACKAGE_DEPLOYABLE = 'packageDeployable'

    @Override
    void applyTo(Project project) {

        project.plugins.apply(PythonPlugin)
        def extension = ExtensionUtils.getPythonExtension(project)
        ExtensionUtils.maybeCreatePexExtension(project)
        ExtensionUtils.maybeCreateWheelExtension(project)
        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)


        project.afterEvaluate {
            if (settings.details.pythonVersion.pythonMajorMinor == '2.6') {
                project.dependencies.add(StandardTextValues.CONFIGURATION_BUILD_REQS.value, extension.forcedVersions['argparse'])
            }
        }
        project.dependencies.add(StandardTextValues.CONFIGURATION_BUILD_REQS.value, extension.forcedVersions['pex'])


        /**
         * Build wheels.
         *
         * We need wheels to build pex files.
         */
        project.tasks.create(TASK_BUILD_WHEELS, BuildWheelsTask) { task ->
            task.dependsOn project.tasks.getByName(StandardTextValues.TASK_INSTALL_PROJECT.value)
        }

        project.tasks.create(TASK_BUILD_PEX, BuildPexTask) { task ->
            task.dependsOn(project.tasks.getByName(TASK_BUILD_WHEELS))
        }

        def packageDeployable = project.tasks.create(TASK_PACKAGE_DEPLOYABLE, Tar, new Action<Tar>() {
            @Override
            void execute(Tar tar) {
                tar.compression = Compression.GZIP
                tar.baseName = project.name
                tar.extension = 'tar.gz'
                tar.from(deployableExtension.deployableBuildDir)
            }
        })
        packageDeployable.dependsOn(project.tasks.getByName(TASK_BUILD_PEX))

        project.artifacts.add(StandardTextValues.CONFIGURATION_DEFAULT.value, packageDeployable)
    }
}
