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


class PythonWheelDistributionPlugin extends PythonBasePlugin {

    public final static String TASK_PACKAGE_WHEEL = 'packageWheel'

    @Override
    void applyTo(Project project) {
        // XXX: This needs to be adjusted to work with a build matrix one day. Until
        // that is ready, we always assume pure Python 2.6 on Linux.
        def wheelArtifact = project.file("${project.projectDir}/dist/${project.name.replace("-", "_")}-${project.spec.version.replace("-", "_")}-py2-none-any.whl")

        /**
         * Create a Python wheel distribution.
         */
        project.tasks.create(TASK_PACKAGE_WHEEL) {
            dependsOn(project.tasks.getByName(PythonPlugin.TASK_INSTALL_PROJECT))
            outputs.file(wheelArtifact)
            doLast {
                project.exec {
                    environment settings.pythonEnvironmentDistgradle
                    commandLine settings.details.virtualEnvInterpreter, "setup.py", "bdist_wheel"
                }
            }
        }
        def wheelArtifactInfo = [
                name: "${project.name.replace("-", "_")}",
                classifier: "py2-none-any",
                type: 'whl',
                file: wheelArtifact,
                builtBy: project.tasks.getByName(TASK_PACKAGE_WHEEL),
        ]
        if (!project.spec.version.contains('SNAPSHOT')) {
          project.artifacts.add(PythonPlugin.CONFIGURATION_WHEEL, wheelArtifactInfo)
        }

    }

}
