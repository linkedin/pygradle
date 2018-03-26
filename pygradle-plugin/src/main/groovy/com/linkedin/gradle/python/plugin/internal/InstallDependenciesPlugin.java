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
package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.PipInstallTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYTHON;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_SETUP_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_TEST;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_PROJECT;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_PYTHON_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_SETUP_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_TEST_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_SETUP_LINKS;

public class InstallDependenciesPlugin implements Plugin<Project> {

    private static final String PIP_UPGRADE = "--upgrade";

    @Override
    public void apply(final Project project) {

        final PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        /*
         * Install core setup requirements into virtualenv.
         *
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        project.getTasks().create(TASK_INSTALL_SETUP_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(TASK_SETUP_LINKS.getValue()));
            it.setArgs(Collections.singletonList(PIP_UPGRADE));
            it.setInstallFileCollection(project.getConfigurations().getByName(CONFIGURATION_SETUP_REQS.getValue()));
            it.setSorted(false);
        });

        /*
         * Install build requirements into virtualenv.
         *
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        project.getTasks().create(TASK_INSTALL_BUILD_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(TASK_INSTALL_SETUP_REQS.getValue()));
            it.setArgs(Collections.singletonList(PIP_UPGRADE));
            it.setInstallFileCollection(project.getConfigurations().getByName(CONFIGURATION_BUILD_REQS.getValue()));
        });

        /*
         * Install the product's Python test requirements.
         *
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        project.getTasks().create(TASK_INSTALL_TEST_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(TASK_INSTALL_BUILD_REQS.getValue()));
            it.setInstallFileCollection(project.getConfigurations().getByName(CONFIGURATION_TEST.getValue()));
        });

        /*
         * Install the product's Python requirements.
         *
         * A products Python requirements are those listed in the python configuration.
         *
         */
        project.getTasks().create(TASK_INSTALL_PYTHON_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            // by running after test reqs we ensure, in case different versions of same req exist in both, that version from python reqs takes precedence.
            it.dependsOn(project.getTasks().getByName(TASK_INSTALL_TEST_REQS.getValue()));
            it.setInstallFileCollection(project.getConfigurations().getByName(CONFIGURATION_PYTHON.getValue()));
        });

        /*
         * Install the product itself.
         *
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        project.getTasks().create(TASK_INSTALL_PROJECT.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(TASK_INSTALL_PYTHON_REQS.getValue()));
            it.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
            it.setEnvironment(settings.pythonEnvironmentDistgradle);
        });
    }

}
