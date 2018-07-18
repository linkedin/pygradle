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
import com.linkedin.gradle.python.exception.PipExecutionException
import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.extension.WheelExtension
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.tasks.action.pip.PipWheelAction
import com.linkedin.gradle.python.tasks.exec.ExternalExec
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings
import com.linkedin.gradle.python.tasks.supports.SupportsWheelCache
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger
import com.linkedin.gradle.python.util.DependencyOrder
import com.linkedin.gradle.python.util.EnvironmentMerger
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.PackageInfo
import com.linkedin.gradle.python.util.PackageSettings
import com.linkedin.gradle.python.util.internal.TaskTimer
import com.linkedin.gradle.python.wheel.EmptyWheelCache
import com.linkedin.gradle.python.wheel.WheelCache
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory

class BuildWheelsTask extends DefaultTask implements SupportsWheelCache, SupportsPackageInfoSettings, FailureReasonProvider {

    @Input
    WheelCache wheelCache = new EmptyWheelCache()

    private PythonExtension pythonExtension
    private PythonDetails details

    @InputFiles
    FileCollection installFileCollection

    @Input
    List<String> args = []

    @Input
    @Optional
    Map<String, String> environment

    PackageSettings<PackageInfo> packageSettings

    EnvironmentMerger environmentMerger = new DefaultEnvironmentMerger()
    ExternalExec externalExec = new ProjectExternalExec(getProject())
    private String lastInstallMessage = null

    public BuildWheelsTask() {
        getOutputs().doNotCacheIf('When package packageExcludeFilter is set', new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task element) {
                return ((BuildWheelsTask) element).packageExcludeFilter != null
            }
        })
    }

    @TaskAction
    void buildWheelsTask() {
        buildWheels(project, DependencyOrder.getConfigurationFiles(installFileCollection), getPythonDetails())

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
    Spec<PackageInfo> packageExcludeFilter = null

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

        def baseEnvironment = environmentMerger.mergeEnvironments([pythonExtension.pythonEnvironment, environment])
        def wheelAction = new PipWheelAction(packageSettings, project, externalExec, baseEnvironment,
            pythonDetails, wheelCache, environmentMerger, wheelExtension)

        def taskTimer = new TaskTimer()

        int counter = 0
        def numberOfInstallables = installables.size()
        installables.each { File installable ->
            def packageInfo = PackageInfo.fromPath(installable)
            def shortHand = packageInfo.toShortHand()

            def clock = taskTimer.start(shortHand)
            progressLogger.progress("Preparing wheel $shortHand (${++counter} of $numberOfInstallables)")

            if (packageExcludeFilter != null && packageExcludeFilter.isSatisfiedBy(packageInfo)) {
                if (PythonHelpers.isPlainOrVerbose(project)) {
                    logger.lifecycle("Skipping {} - Excluded", packageInfo.toShortHand())
                }
            } else {
                try {
                    wheelAction.buildWheel(packageInfo, args)
                } catch (PipExecutionException e) {
                    lastInstallMessage = e.pipText
                    throw e
                }
            }

            clock.stop()
        }

        progressLogger.completed()

        new File(project.buildDir, getName() + "-task-runtime-report.txt").text = taskTimer.buildReport()
    }

    @Override
    String getReason() {
        return lastInstallMessage
    }
}
