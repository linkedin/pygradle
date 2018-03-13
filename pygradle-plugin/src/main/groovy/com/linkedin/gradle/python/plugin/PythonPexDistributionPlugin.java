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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.tasks.BuildPexTask;
import com.linkedin.gradle.python.tasks.BuildWheelsTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;

public class PythonPexDistributionPlugin extends PythonBasePlugin {

    public static final String TASK_BUILD_WHEELS = "buildWheels";
    public static final String TASK_BUILD_PROJECT_WHEEL = "buildProjectWheel";
    public static final String TASK_BUILD_PEX = "buildPex";
    public static final String TASK_PACKAGE_DEPLOYABLE = "packageDeployable";

    @Override
    public void applyTo(final Project project) {

        project.getPlugins().apply(PythonPlugin.class);
        final PythonExtension extension = ExtensionUtils.getPythonExtension(project);
        ExtensionUtils.maybeCreatePexExtension(project);
        ExtensionUtils.maybeCreateWheelExtension(project);
        final DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project);

        project.afterEvaluate(ignored -> {
            if (settings.getDetails().getPythonVersion().getPythonMajorMinor().equals("2.6")) {
                project.getDependencies().add(StandardTextValues.CONFIGURATION_BUILD_REQS.getValue(),
                    extension.forcedVersions.get("argparse"));
            }
        });

        project.getDependencies().add(StandardTextValues.CONFIGURATION_BUILD_REQS.getValue(),
            extension.forcedVersions.get("pex"));


        /*
         * Build wheels.
         *
         * We need wheels to build pex files.
         */
        project.getTasks().create(TASK_BUILD_WHEELS, BuildWheelsTask.class, task -> {
            task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue()));
            task.setInstallFileCollection(project.getConfigurations().getByName("python"));
        });

        project.getTasks().create(TASK_BUILD_PROJECT_WHEEL, BuildWheelsTask.class, task -> {
            task.dependsOn(project.getTasks().getByName(TASK_BUILD_WHEELS));
            task.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
            task.setEnvironment(extension.pythonEnvironmentDistgradle);
        });

        project.getTasks().create(TASK_BUILD_PEX, BuildPexTask.class,
            task -> task.dependsOn(project.getTasks().getByName(TASK_BUILD_PROJECT_WHEEL)));

        Tar packageDeployable = project.getTasks().create(TASK_PACKAGE_DEPLOYABLE, Tar.class, tar -> {
            tar.setCompression(Compression.GZIP);
            tar.setBaseName(project.getName());
            tar.setExtension("tar.gz");
            tar.from(deployableExtension.getDeployableBuildDir());
        });
        packageDeployable.dependsOn(project.getTasks().getByName(TASK_BUILD_PEX));

        project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), packageDeployable);
    }
}
