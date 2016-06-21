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
import com.linkedin.gradle.python.plugin.PythonHelpers
import com.linkedin.gradle.python.util.ConsoleOutput
import com.linkedin.gradle.python.util.MiscUtils
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.time.Duration
import java.time.LocalDateTime

/**
 * Execute pip install
 *
 * TODO: Add an output to make execution faster
 */
class PipInstallTask extends DefaultTask {

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
     * Returns a set of configuration files in the insert order or sorted.
     *
     * If sorted is true (default) the sorted configuration set is returned,
     * otherwise the original order.
     */
    Set<File> getConfigurationFiles() {
        return sorted ? installFileCollection.files.sort() : installFileCollection.files
    }

    /**
     * Install things into a virtual environment.
     * <p>
     * Always run <code>pip install --no-deps <args></code> with arguments. This prevents
     * naughty dependencies from using pure-Setuptools and mppy machinery to
     * express and download dependencies.
     */
    @TaskAction
    public void pipInstall() {

        PythonExtension settings = project.getExtensions().getByType(PythonExtension)

        def pyVersion = settings.getDetails().getPythonVersion().pythonMajorMinor

        getConfigurationFiles().each { File installable ->

            def (String name, String version) = MiscUtils.packageInfoFromPath(installable.getAbsolutePath())
            String sanitizedName = name.replace('-', '_')

            // See: https://www.python.org/dev/peps/pep-0376/
            File egg = new File(settings.getDetails().virtualEnv, "lib/python${pyVersion}/site-packages/${sanitizedName}-${version}-py${pyVersion}.egg-info")
            File dist = new File(settings.getDetails().virtualEnv, "lib/python${pyVersion}/site-packages/${sanitizedName}-${version}.dist-info")

            def mergedEnv = new HashMap(settings.pythonEnvironment)
            if (environment != null) {
                mergedEnv.putAll(environment)
            }

            def commandLine = [VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                               VirtualEnvExecutableHelper.getPip(settings),
                               'install',
                               '--disable-pip-version-check',
                               '--no-deps']
            commandLine.addAll(args)
            commandLine.add(installable.getAbsolutePath())

            def shortHand = version ? "${name}-${version}" : name

            if (project.file(egg).exists() || project.file(dist).exists()) {
                logger.lifecycle(PythonHelpers.createPrettyLine("Install ${shortHand}", "[SKIPPING]"))
                return
            }

            logger.lifecycle(PythonHelpers.createPrettyLine("Install ${shortHand}", "[STARTING]"))

            def startTime = LocalDateTime.now()
            def stream = new ByteArrayOutputStream()
            ExecResult installResult = project.exec { ExecSpec execSpec ->
                execSpec.environment mergedEnv
                execSpec.commandLine(commandLine)
                execSpec.standardOutput = stream
                execSpec.errorOutput = stream
                execSpec.ignoreExitValue = true
            }
            def endTime = LocalDateTime.now()
            def duration = Duration.between(startTime, endTime)

            if (installResult.exitValue != 0) {
                /*
                 * TODO: maintain a list of packages that failed to install, and report a failure
                 * report at the end. We can leverage our domain expertise here to provide very
                 * meaningful errors. E.g., we see lxml failed to install, do you have libxml2
                 * installed? E.g., we see pyOpenSSL>0.15 failed to install, do you have libffi
                 * installed?
                 */
                println(stream.toString().trim())
                throw new GradleException(
                    "Failed to install ${shortHand}. Please see above output for reason, or re-run your build using ``ligradle -i build`` for additional logging.")
            } else {
                if (settings.consoleOutput == ConsoleOutput.RAW) {
                    logger.lifecycle(stream.toString().trim())
                } else {
                    String prefix = String.format("Install %s (%d:%02d s)", shortHand, duration.toMinutes(), duration.getSeconds() % 60)
                    logger.lifecycle(PythonHelpers.createPrettyLine(prefix, "[FINISHED]"))
                }
            }
        }
    }
}
