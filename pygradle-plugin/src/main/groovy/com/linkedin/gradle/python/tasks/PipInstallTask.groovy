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
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider
import com.linkedin.gradle.python.util.ConsoleOutput
import com.linkedin.gradle.python.util.DependencyOrder
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.OperatingSystem
import com.linkedin.gradle.python.util.PackageInfo
import com.linkedin.gradle.python.util.internal.TaskTimer
import com.linkedin.gradle.python.wheel.EmptyWheelCache
import com.linkedin.gradle.python.wheel.SupportsWheelCache
import com.linkedin.gradle.python.wheel.WheelCache
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Execute pip install
 *
 * TODO: Add an output to make execution faster
 */
@CompileStatic
class PipInstallTask extends DefaultTask implements FailureReasonProvider, SupportsWheelCache {

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

    private String lastInstallMessage = null

    /**
     * Returns a set of configuration files in the insert order or sorted.
     *
     * If sorted is true (default) the sorted configuration set is returned,
     * otherwise the original order.
     */
    Collection<File> getConfigurationFiles() {
        if (sorted && (installFileCollection instanceof Configuration)) {
            try {
                return DependencyOrder.configurationPostOrderFiles((Configuration) installFileCollection)
            } catch (Throwable e) {
                // Log and fall back to old style installation order as before.
                logger.lifecycle("***** WARNING: ${ e.message } *****")
            }
        }
        return sorted ? installFileCollection.files.sort() : installFileCollection.files
    }

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
        def pyVersion = pythonDetails.getPythonVersion().pythonMajorMinor
        def extension = ExtensionUtils.getPythonExtension(project)
        def sitePackages = findSitePackages()

        ProgressLoggerFactory progressLoggerFactory = getServices().get(ProgressLoggerFactory)
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(PipInstallTask)
        progressLogger.setDescription("Installing Libraries")

        progressLogger.started()
        TaskTimer taskTimer = new TaskTimer()

        int counter = 0
        def installableFiles = getConfigurationFiles()
        for (File installable : installableFiles) {
            if (isReadyForInstall(installable)) {
                def packageInfo = PackageInfo.fromPath(installable.getAbsolutePath())
                String shortHand = packageInfo.version ? "${ packageInfo.name }-${ packageInfo.version }" : packageInfo.name

                def timer = taskTimer.start(shortHand)
                logger.info("Installing {}", shortHand)
                progressLogger.progress("Installing $shortHand (${ ++counter } of ${ installableFiles.size() })")
                doInstall(shortHand, packageInfo, sitePackages, pyVersion, extension, installable)
                timer.stop()
            }
        }

        progressLogger.completed()

        new File(project.buildDir, getName() + "-task-runtime-report.txt").text = taskTimer.buildReport()
    }

    @SuppressWarnings("ParameterCount")
    private void doInstall(String shortHand, PackageInfo packageInfo, Path sitePackages,
                           String pyVersion, PythonExtension extension, File installable) {
        if (packageExcludeFilter.isSatisfiedBy(packageInfo)) {
            return
        }

        if (extension.consoleOutput == ConsoleOutput.RAW) {
            logger.lifecycle("Installing {}", shortHand)
        }

        String sanitizedName = packageInfo.name.replace('-', '_')

        // See: https://www.python.org/dev/peps/pep-0376/
        File egg = sitePackages.resolve("${ sanitizedName }-${ packageInfo.version }-py${ pyVersion }.egg-info").toFile()
        File dist = sitePackages.resolve("${ sanitizedName }-${ packageInfo.version }.dist-info").toFile()

        def mergedEnv = new HashMap(extension.pythonEnvironment)
        if (environment != null) {
            mergedEnv.putAll(environment)
        }

        if (project.file(egg).exists() || project.file(dist).exists()) {
            return
        }

        def commandLine = [pythonDetails.getVirtualEnvInterpreter(),
                           pythonDetails.getVirtualEnvironment().getPip(),
                           'install',
                           '--disable-pip-version-check',
                           '--no-deps']
        commandLine.addAll(args)

        if (shortHand.endsWith('-SNAPSHOT')) {
            // snapshot packages may have changed, so reinstall them every time
            commandLine.add('--ignore-installed')
        }


        def cachedWheel = wheelCache.findWheel(packageInfo.name, packageInfo.version, pythonDetails)
        if (cachedWheel.isPresent()) {
            commandLine.add(cachedWheel.get().getAbsolutePath())
        } else {
            commandLine.add(installable.getAbsolutePath())
        }

        def stream = new ByteArrayOutputStream()
        ExecResult installResult = project.exec { ExecSpec execSpec ->
            execSpec.environment mergedEnv
            execSpec.commandLine(commandLine)
            execSpec.standardOutput = stream
            execSpec.errorOutput = stream
            execSpec.ignoreExitValue = true
        }

        def message = stream.toString().trim()
        if (installResult.exitValue != 0) {
            /*
             * TODO: maintain a list of packages that failed to install, and report a failure
             * report at the end. We can leverage our domain expertise here to provide very
             * meaningful errors. E.g., we see lxml failed to install, do you have libxml2
             * installed? E.g., we see pyOpenSSL>0.15 failed to install, do you have libffi
             * installed?
             */
            logger.lifecycle(message)
            lastInstallMessage = message

            throw new PipInstallException(
                "Failed to install ${ shortHand }. Please see above output for reason, or re-run your build using ``gradle -i build`` for additional logging.")
        } else {
            if (extension.consoleOutput == ConsoleOutput.RAW) {
                logger.lifecycle(message)
            }
        }
    }

    public static class PipInstallException extends GradleException {
        public PipInstallException(String message) {
            super(message)
        }
    }

    private Path findSitePackages() {
        def pyVersion = pythonDetails.getPythonVersion().pythonMajorMinor
        if (OperatingSystem.current().isUnix()) {
            return pythonDetails.virtualEnv.toPath().resolve(Paths.get("lib", "python${ pyVersion }", "site-packages"))
        } else {
            return pythonDetails.virtualEnv.toPath().resolve(Paths.get("Lib", "site-packages"))
        }
    }

    @Override
    String getReason() {
        return lastInstallMessage
    }
}
