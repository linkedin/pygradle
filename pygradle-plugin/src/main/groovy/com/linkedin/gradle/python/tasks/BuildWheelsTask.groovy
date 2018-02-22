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
import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.extension.WheelExtension
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.util.DependencyOrder
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.PackageInfo
import com.linkedin.gradle.python.util.internal.TaskTimer
import com.linkedin.gradle.python.wheel.EmptyWheelCache
import com.linkedin.gradle.python.wheel.SupportsWheelCache
import com.linkedin.gradle.python.wheel.WheelCache
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

class BuildWheelsTask extends DefaultTask implements SupportsWheelCache {

    private static final Logger LOGGER = Logging.getLogger(BuildWheelsTask)

    @Input
    WheelCache wheelCache = new EmptyWheelCache()

    private PythonExtension pythonExtension
    private PythonDetails details

    @TaskAction
    public void buildWheelsTask() {
        Collection<File> configurationFiles = null
        try {
            configurationFiles = DependencyOrder.configurationPostOrderFiles(project.configurations.python)
        } catch (Throwable e) {
            // Log and fall back to old style installation order as before.
            logger.lifecycle("***** WARNING: ${ e.message } *****")
            configurationFiles = project.configurations.python.files.sort()
        }
        buildWheels(project, configurationFiles, getPythonDetails())

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
            if (getPythonDetails().pythonVersion.pythonMajorMinor == '2.6' && file.name.contains('argparse')) {
                pexDependencies.add(file)
            }
        }
        buildWheels(project, pexDependencies.sort(), getPythonDetails())
    }

    /**
     * Will return true when the package should be excluded from being installed.
     */
    @Input
    Spec<PackageInfo> packageExcludeFilter = new Spec<PackageInfo>() {
        @Override
        boolean isSatisfiedBy(PackageInfo packageInfo) {
            return false
        }
    }

    @Input
    PythonDetails getPythonDetails() {
        if (null == details) {
            details = getPythonExtension().details
        }

        return details
    }

    @Input
    PythonExtension getPythonExtension() {
        if (null == pythonExtension) {
            pythonExtension = ExtensionUtils.getPythonExtension(project)
        }
        return pythonExtension
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
    private void buildWheels(Project project, Collection<File> installables, PythonDetails pythonDetails) {

        ProgressLoggerFactory progressLoggerFactory = getServices().get(ProgressLoggerFactory)
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(BuildWheelsTask)
        progressLogger.setDescription("Building Wheels")
        progressLogger.started()

        WheelExtension wheelExtension = ExtensionUtils.getPythonComponentExtension(project, WheelExtension)
        def pythonExtension = ExtensionUtils.getPythonExtension(project)

        def taskTimer = new TaskTimer()

        int counter = 0
        def numberOfInstallables = installables.size()
        installables.each { File installable ->

            def packageInfo = PackageInfo.fromPath(installable.path)
            def shortHand = packageInfo.version ? "${ packageInfo.name }-${ packageInfo.version }" : packageInfo.name

            def clock = taskTimer.start(shortHand)
            progressLogger.progress("Preparing wheel $shortHand (${ ++counter } of $numberOfInstallables)")

            if (PythonHelpers.isPlainOrVerbose(project)) {
                LOGGER.lifecycle("Installing {}", shortHand)
            }

            if (packageExcludeFilter.isSatisfiedBy(packageInfo)) {
                return
            }

            def wheel = wheelCache.findWheel(packageInfo.name, packageInfo.version, pythonExtension.details)
            if (wheel.isPresent()) {
                FileUtils.copyFile(wheel.get(), new File(wheelExtension.wheelCache, wheel.get().name))
                return
            }

            // Check if a wheel exists for this product already and only build it
            // if it is missing. We don't care about the wheel details because we
            // always build these locally.
            def tree = project.fileTree(
                dir: wheelExtension.wheelCache,
                include: "**/${ packageInfo.name.replace('-', '_') }-${ packageInfo.version }-*.whl")

            def stream = new ByteArrayOutputStream()

            if (tree.files.size() >= 1) {
                return
            }

            def commandLine = [pythonDetails.getVirtualEnvInterpreter(),
                           pythonDetails.getVirtualEnvironment().getPip(),
                           'wheel',
                           '--disable-pip-version-check',
                           '--wheel-dir', wheelExtension.wheelCache,
                           '--no-deps',
                           installable]

            ExecResult installResult = project.exec { ExecSpec execSpec ->
                execSpec.environment pythonExtension.pythonEnvironment
                execSpec.commandLine(commandLine)
                execSpec.standardOutput = stream
                execSpec.errorOutput = stream
                execSpec.ignoreExitValue = true
            }

            if (installResult.exitValue != 0) {
                LOGGER.error("Error installing package using `{}`", commandLine)
                LOGGER.error(stream.toString().trim())
                throw new GradleException("Failed to build wheel for ${ shortHand }. Please see above output for reason, or re-run your build using ``--info`` for additional logging.")
            } else {
                LOGGER.info(stream.toString().trim())
            }

            clock.stop()
        }

        progressLogger.completed()

        new File(project.buildDir, getName() + "-task-runtime-report.txt").text = taskTimer.buildReport()
    }
}
