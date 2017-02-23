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

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask
import com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask
import com.linkedin.gradle.python.tasks.CleanSaveVenvTask
import com.linkedin.gradle.python.tasks.InstallVirtualEnvironmentTask
import com.linkedin.gradle.python.tasks.PinRequirementsTask
import com.linkedin.gradle.python.tasks.PipInstallTask
import com.linkedin.gradle.python.util.FileSystemUtils
import org.gradle.api.Project
import org.gradle.api.Task

import static com.linkedin.gradle.python.util.StandardTextValuesConfiguration.*
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.CLEAN_SAVE_VENV
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_BUILD_REQS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_PROJECT
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_PYTHON_REQS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_SETUP_REQS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_TEST_REQS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.PIN_REQUIREMENTS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.SETUP_LINKS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.VENV_CREATE

class PythonVenvPlugin extends AbstractPluginBase {

    @Override
    void apply(Project project) {
        this.project = project

        createConfigurations()

        PythonExtension settings = addGetExtensionLocal(PYTHON_EXTENSION_NAME, PythonExtension)

        createDependenciesVenv(settings)

        addTaskLocal([name: PIN_REQUIREMENTS, type: PinRequirementsTask])

        addTaskLocal([name: VENV_CREATE, type: InstallVirtualEnvironmentTask]) { task ->
            task.pythonDetails = settings.details
        }

        addTaskLocal([name: SETUP_LINKS]) {
            outputs.file(settings.getDetails().activateLink)

            doLast {
                def activateLinkSource = settings.getDetails().virtualEnvironment.getScript("activate")
                def activateLink = settings.getDetails().activateLink
                FileSystemUtils.makeSymLink(activateLinkSource, activateLink)
            }
        }

        addTaskLocal([name: CLEAN_SAVE_VENV, type: CleanSaveVenvTask])

        /**
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        addTaskLocal([name: INSTALL_SETUP_REQS, type: PipInstallTask]) {
            pythonDetails = settings.details
            args = ['--upgrade']
            installFileCollection = project.configurations.setupRequires
            sorted = false
        }

        /**
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        addTaskLocal([name: INSTALL_BUILD_REQS, type: PipInstallTask]) {
            pythonDetails = settings.details
            args = ['--upgrade']
            installFileCollection = project.configurations.build
        }

        /**
         * A products Python requirements are those listed in the product-spec.json in the external or product sections.
         */
        addTaskLocal([name: INSTALL_PYTHON_REQS, type: PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.configurations.python
        }

        /**
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        addTaskLocal([name: INSTALL_TEST_REQS, type: PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.configurations.test
        }

        /**
         * Any task that extends from {@link com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask} will require INSTALL_BUILD_REQS
         */
        project.tasks.withType(AbstractPythonMainSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(INSTALL_BUILD_REQS.value)
        }

        /**
         * Any task that extends from {@link com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask} will require INSTALL_PROJECT
         */
        project.tasks.withType(AbstractPythonTestSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(INSTALL_PROJECT.value)
        }

        aDependsOnB(VENV_CREATE, PIN_REQUIREMENTS)
        aDependsOnB(SETUP_LINKS, VENV_CREATE)
        aDependsOnB(INSTALL_SETUP_REQS, SETUP_LINKS)
        aDependsOnB(INSTALL_BUILD_REQS, INSTALL_SETUP_REQS)
        aDependsOnB(INSTALL_PYTHON_REQS, INSTALL_BUILD_REQS)
        aDependsOnB(INSTALL_TEST_REQS, INSTALL_PYTHON_REQS)
        aDependsOnB(INSTALL_PROJECT, INSTALL_TEST_REQS)

    }

    def createConfigurations() {
        def pythonConf = createConfiguration(PYTHON)
        /*
         * To resolve transitive dependencies, we need the 'default' configuration
         * to extend the 'python' configuration. This is because the source
         * configuration must match the configuration which the artifact is
         * published to (i.e., 'default' in our case).
         */
        project.configurations.'default'.extendsFrom(pythonConf)

        createConfiguration(BOOTSTRAP_REQS)
        createConfiguration(SETUP_REQS)
        createConfiguration(BUILD_REQS)
        createConfiguration(TEST)
        createConfiguration(VENV)
        createConfiguration(WHEEL)

        /*
         * We must extend test configuration from the python configuration.
         *
         * 1. This prevents test dependencies overwriting python dependencies
         *    if they resolve to a different version transitively.
         * 2. Since tests do depend on all runtime dependencies, this is also
         *    semantically correct.
         */
        project.configurations.test.extendsFrom(pythonConf)
    }
}
