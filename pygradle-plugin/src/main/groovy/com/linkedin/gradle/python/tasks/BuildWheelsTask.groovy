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
package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.extension.WheelExtension
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.util.ConsoleOutput
import com.linkedin.gradle.python.util.PackageInfo
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.time.Duration
import java.time.LocalDateTime

class BuildWheelsTask extends DefaultTask {

    private static final Logger LOGGER = Logging.getLogger(BuildWheelsTask)

    @TaskAction
    public void buildWheelsTask() {
        PythonExtension settings = project.getExtensions().getByType(PythonExtension)
        buildWheels(project, project.configurations.python.files, settings)

        /*
         * If pexDependencies are empty or its wheels are already
         * installed from python configuration, the call below will
         * have no effect.
         */
        List<File> pexDependencies = []

        /*
         * In Python <=2.6, argparse is not part of the standard library
         * and Pex requires it, so we need to include it as a dependency
         */
        project.configurations.build.files.each { file ->
            if (settings.details.pythonVersion.pythonMajorMinor == '2.6' && file.name.contains('argparse')) {
                pexDependencies.add(file)
            }
        }
        buildWheels(project, pexDependencies, settings)
    }

    /**
     * A helper function that builds wheels.
     * <p>
     * This function consumes a list of paths to Python packages and builds
     * wheels for each of them. Dependencies of the Python packages are not
     * installed.
     * <p>
     * @param project The project to run within.
     * @param installables A collection of Python source distributions to compile as wheels.
     * @param env The environment to pass along to <pre>pip</pre>.
     */
    protected static void buildWheels(Project project, Collection<File> installables, PythonExtension settings) {

        WheelExtension wheelExtension = (settings as ExtensionAware).getExtensions().findByType(WheelExtension)

        installables.sort().each { File installable ->

            def packageInfo = PackageInfo.fromPath(installable.path)

            // Check if a wheel exists for this product already and only build it
            // if it is missing. We don't care about the wheel details because we
            // always build these locally.
            def tree = project.fileTree(
                    dir: wheelExtension.wheelCache,
                    include: "**/${packageInfo.name.replace('-', '_')}-${packageInfo.version}-*.whl")

            def stream = new ByteArrayOutputStream()

            def shortHand = packageInfo.version ? "${packageInfo.name}-${packageInfo.version}" : packageInfo.name

            def messageHead = 'Preparing wheel ' + shortHand

            if (tree.files.size() >= 1) {
                LOGGER.lifecycle(PythonHelpers.createPrettyLine(messageHead, "[SKIPPING]"))
                return
            }

            LOGGER.lifecycle(PythonHelpers.createPrettyLine(messageHead, "[STARTING]"))

            def startTime = LocalDateTime.now()
            ExecResult installResult = project.exec { ExecSpec execSpec ->
                execSpec.environment settings.pythonEnvironment
                execSpec.commandLine(
                    [VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                     VirtualEnvExecutableHelper.getPip(settings),
                     'wheel',
                     '--disable-pip-version-check',
                     '--wheel-dir', wheelExtension.wheelCache,
                     '--no-deps',
                     installable
                ])
                execSpec.standardOutput = stream
                execSpec.errorOutput = stream
                execSpec.ignoreExitValue = true
            }
            def endTime = LocalDateTime.now()
            def duration = Duration.between(startTime, endTime)

            if (installResult.exitValue != 0) {
                LOGGER.error(stream.toString().trim())
                throw new GradleException("Failed to build wheel for ${shortHand}. Please see above output for reason, or re-run your build using ``--info`` for additional logging.")
            } else {
                if (settings.consoleOutput == ConsoleOutput.RAW) {
                    LOGGER.lifecycle(stream.toString().trim())
                } else {
                    String prefix = String.format(messageHead + ' (%d:%02d s)', duration.toMinutes(), duration.getSeconds() % 60)
                    LOGGER.lifecycle(PythonHelpers.createPrettyLine(prefix, "[FINISHED]"))
                }
            }
        }

    }
}
