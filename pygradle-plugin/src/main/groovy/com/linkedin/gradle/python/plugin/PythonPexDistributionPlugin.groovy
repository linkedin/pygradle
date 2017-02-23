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
import com.linkedin.gradle.python.util.StandardTextValuesConfiguration
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import static com.linkedin.gradle.python.util.StandardTextValuesTasks.*

class PythonPexDistributionPlugin extends PythonBasePlugin {

    @Override
    void applyTo(Project project) {
        this.project = project

        project.plugins.apply(PythonPlugin)

        def extension = ExtensionUtils.getPythonExtension(project)
        ExtensionUtils.maybeCreatePexExtension(project)
        ExtensionUtils.maybeCreateWheelExtension(project)
        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)


        project.afterEvaluate {
            if (settings.details.pythonVersion.pythonMajorMinor == '2.6') {
                project.dependencies.add(StandardTextValuesConfiguration.BUILD_REQS.value, extension.forcedVersions['argparse'])
            }
        }
        project.dependencies.add(StandardTextValuesConfiguration.BUILD_REQS.value, extension.forcedVersions['pex'])

        /**
         * Build wheels.
         *
         * We need wheels to build pex files.
         */
        addTaskLocal(['name': BUILD_WHEELS, 'type': BuildWheelsTask])
        addTaskLocal(['name': BUILD_PEX, 'type': BuildPexTask])

        def packageDeployable = addTaskLocal(['name': PACKAGE_DEPLOYABLE, 'type': Tar, 'action': new Action<Tar>() {
            @Override
            void execute(Tar tar) {
                tar.compression = Compression.GZIP
                tar.baseName = project.name
                tar.extension = 'tar.gz'
                tar.from(deployableExtension.deployableBuildDir)
            }
        }])

        project.artifacts.add(StandardTextValuesConfiguration.DEFAULT.value, packageDeployable)

        // now that everything is defined, do the depends.
        aDependsOnB(BUILD_WHEELS, INSTALL_PROJECT)
        aDependsOnB(BUILD_PEX, BUILD_WHEELS)
        aDependsOnB(PACKAGE_DEPLOYABLE, BUILD_PEX)
    }
}
