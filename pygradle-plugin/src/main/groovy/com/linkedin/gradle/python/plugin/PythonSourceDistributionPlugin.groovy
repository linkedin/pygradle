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


import com.linkedin.gradle.python.tasks.SourceDistTask
import com.linkedin.gradle.python.util.StandardTextValuesConfiguration
import com.linkedin.gradle.python.util.StandardTextValuesTasks
import org.gradle.api.Project

import static com.linkedin.gradle.python.util.StandardTextValuesTasks.PACKAGE_SDIST

class PythonSourceDistributionPlugin extends PythonBasePlugin {

    @Override
    void applyTo(Project project) {

        /**
         * Create a Python source distribution.
         */
        def sdistPackageTask = project.tasks.create(PACKAGE_SDIST.value, SourceDistTask) {
            dependsOn(project.tasks.getByName(StandardTextValuesTasks.INSTALL_PROJECT.value))
        }

        def sdistArtifactInfo = [
                name: project.name,
                type: 'tgz',
                extension: 'tar.gz',
                file: sdistPackageTask.getSdistOutput(),
                builtBy: project.tasks.getByName(PACKAGE_SDIST.value),
        ]

        project.artifacts.add(StandardTextValuesConfiguration.DEFAULT.value, sdistArtifactInfo)
    }

}
