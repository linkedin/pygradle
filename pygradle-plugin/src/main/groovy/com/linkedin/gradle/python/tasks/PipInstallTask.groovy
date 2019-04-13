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

import com.linkedin.gradle.python.exception.PipExecutionException
import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.tasks.action.pip.PipInstallAction
import com.linkedin.gradle.python.tasks.exec.ExternalExec
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider
import com.linkedin.gradle.python.tasks.supports.SupportsPackageFiltering
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
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory

/**
 * Execute pip install
 *
 * TODO: Add an output to make execution faster
 */
@CompileStatic
class PipInstallTask extends DefaultTask implements FailureReasonProvider, SupportsWheelCache,
    SupportsPackageInfoSettings, SupportsPackageFiltering {

    @Input
    WheelCache wheelCache = new EmptyWheelCache()

    @Input
    PythonDetails pythonDetails

    @InputFiles
    FileCollection installFileCollection

    @Input
    List<String> args = []

    @Input
    @Optional
    Map<String, String> environment

    @Input
    @Optional
    boolean sorted = true

    PackageSettings<PackageInfo> packageSettings
    EnvironmentMerger environmentMerger = new DefaultEnvironmentMerger()
    ExternalExec externalExec = new ProjectExternalExec(getProject())

    /**
     * Will return true when the package should be excluded from being installed.
     */
    Spec<PackageInfo> packageExcludeFilter = null

    private String lastInstallMessage = null

    /**
     * Method that checks to ensure that the current project is prepared to pip install.  It ignores the
     * base pygradle libraries
     *
     * @param installable file object for the system to install
     * @return
     */
    private boolean isReadyForInstall(File installable) {
        if (installable.absolutePath == project.projectDir.absolutePath) {
            /*
            we are installing the product itself.  Now lets see if its ready for it
            this ignores dependencies
             */
            def setupPyFile = new File(installable.absolutePath, "setup.py")
            if (!setupPyFile.exists()) {
                logger.lifecycle(PythonHelpers.createPrettyLine("Install ${ project.name }", "[ABORTED]"))
                project.logger.warn("setup.py missing, skipping venv install for product ${ project.name }.  Run 'gradle generateSetupPy' to generate a generic file")
                return false
            }
        }
        return true
    }

    /**
     * Install things into a virtual environment.
     * <p>
     * Always run <code>pip install --no-deps <args></code> with arguments. This prevents
     * naughty dependencies from using pure-Setuptools and mppy machinery to
     * express and download dependencies.
     */
    @TaskAction
    void pipInstall() {
        def extension = ExtensionUtils.getPythonExtension(project)

        ProgressLoggerFactory progressLoggerFactory = (ProgressLoggerFactory) getServices().get(ProgressLoggerFactory)
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(PipInstallTask)
        progressLogger.setDescription("Installing Libraries")

        progressLogger.started()
        TaskTimer taskTimer = new TaskTimer()
        def baseEnvironment = environmentMerger.mergeEnvironments([extension.pythonEnvironment, environment])
        def pipInstallAction = new PipInstallAction(packageSettings, project, externalExec,
            baseEnvironment, pythonDetails, wheelCache, environmentMerger, packageExcludeFilter)

        int counter = 0
        def installableFiles = DependencyOrder.getConfigurationFiles(installFileCollection, sorted)
        for (File installable : installableFiles) {
            if (isReadyForInstall(installable)) {
                def packageInfo = PackageInfo.fromPath(installable)

                def shortHand = packageInfo.toShortHand()
                def timer = taskTimer.start(shortHand)
                progressLogger.progress("Installing ${ shortHand } (${ ++counter } of ${ installableFiles.size() })")

                try {
                    pipInstallAction.execute(packageInfo, args)
                } catch (PipExecutionException e) {
                    lastInstallMessage = e.pipText
                    throw e
                }

                timer.stop()
            }
        }

        progressLogger.completed()

        new File(project.buildDir, getName() + "-task-runtime-report.txt").text = taskTimer.buildReport()
    }

    @Override
    String getReason() {
        return lastInstallMessage
    }
}
