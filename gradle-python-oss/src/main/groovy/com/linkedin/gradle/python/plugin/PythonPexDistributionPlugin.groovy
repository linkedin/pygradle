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
import com.linkedin.gradle.python.tasks.BuildWheelsTask
import com.linkedin.gradle.python.util.EntryPointHelpers
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.PexFileUtil
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
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
        PexExtension pexExtension = ExtensionUtils.maybeCreatePexExtension(project)
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(project)
        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)


        project.afterEvaluate {
            if (settings.details.pythonVersion.pythonMajorMinor == '2.6') {
                project.dependencies.add(PythonPlugin.CONFIGURATION_BUILD_REQS,
                        PythonPlugin.PINNED_VERSIONS['argparse'])
            }
        }
        project.dependencies.add(PythonPlugin.CONFIGURATION_BUILD_REQS, PythonPlugin.PINNED_VERSIONS['pex'])

        def pexSource = project.file("${deployableExtension.deployableBinDir}/${project.name}.pex").path

        // Recreate the pex cache if it exists so that we don't mistakenly use an old build's version of the local project.
        if (project.file(pexExtension.pexCache).exists()) {
            project.file(pexExtension.pexCache).deleteDir()
            project.file(pexExtension.pexCache).mkdirs()
        }

        /**
         * Build wheels.
         *
         * We need wheels to build pex files.
         */
        project.tasks.create(TASK_BUILD_WHEELS, BuildWheelsTask) { task ->
            task.dependsOn project.tasks.getByName(PythonPlugin.TASK_INSTALL_PROJECT)
        }

        project.tasks.create(TASK_BUILD_PEX) { task ->

            task.dependsOn(project.tasks.getByName(TASK_BUILD_WHEELS))

            task.doLast {
                project.exec { exec ->
                    exec.environment settings.pythonEnvironmentDistgradle
                    exec.commandLine([VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                                      VirtualEnvExecutableHelper.getPip(settings),
                                      'wheel',
                                      '--disable-pip-version-check',
                                      '--wheel-dir', wheelExtension.wheelCache,
                                      '--no-deps',
                                      '.'])
                }

                project.file(deployableExtension.deployableBuildDir).mkdirs()

                if (pexExtension.fatPex) {
                    // For each entry point, build a stand alone pex file
                    EntryPointHelpers.collectEntryPoints(project).each {
                        println "Processing entry point: ${it}"
                        def (String name, String entry) = it.split('=')*.trim()
                        PexFileUtil.buildPexFile(project,
                                pexExtension.pexCache,
                                new File(deployableExtension.deployableBinDir, name).path,
                                wheelExtension.wheelCache,
                                settings.details.virtualEnvInterpreter.absolutePath,
                                entry)
                    }
                } else {
                    // Build a single stand alone pex file
                    PexFileUtil.buildPexFile(project,
                            pexExtension.pexCache,
                            project.file(pexSource).path,
                            wheelExtension.wheelCache,
                            settings.details.virtualEnvInterpreter.absolutePath,
                            null)
                    // For each entry point, write a thin wrapper
                    EntryPointHelpers.collectEntryPoints(project).each {
                        println "Processing entry point: ${it}"
                        def (String name, String entry) = it.split('=')*.trim()
                        EntryPointHelpers.writeEntryPointScript(project, project.file("${deployableExtension.deployableBinDir}/${name}").path, entry)
                    }
                }
            }
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

        project.artifacts.add(PythonPlugin.CONFIGURATION_DEFAULT, packageDeployable)
    }
}
