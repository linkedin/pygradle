package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.PythonComponent
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

        PythonComponent settings = project.getExtensions().getByType(PythonComponent)

        def pyVersion = settings.getPythonDetails().getPythonVersion().pythonMajorMinor

        getConfigurationFiles().each { File installable ->

            def (String name, String version) = MiscUtils.packageInfoFromPath(installable.getAbsolutePath())
            String sanitizedName = name.replace('-', '_')

            // See: https://www.python.org/dev/peps/pep-0376/
            String egg = "${settings.getEnvironment().virtualEnv}/lib/python${pyVersion}/site-packages/${sanitizedName}-${version}-py${pyVersion}.egg-info"
            String dist = "${settings.getEnvironment().virtualEnv}/lib/python${pyVersion}/site-packages/${sanitizedName}-${version}.dist-info"

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

            new ByteArrayOutputStream().withStream { output ->
                if (!(project.file(egg).exists() || project.file(dist).exists())) {
                    ExecResult installResult = project.exec { ExecSpec execSpec ->
                        execSpec.environment mergedEnv
                        execSpec.commandLine(commandLine)
                        execSpec.standardOutput = output
                        execSpec.errorOutput = output
                        execSpec.ignoreExitValue = true
                    }
                    def shortHand = version ? "${name}-${version}" : name
                    if (installResult.exitValue != 0) {
                        /*
                         * TODO: maintain a list of packages that failed to install, and report a failure
                         * report at the end. We can leverage our domain expertise here to provide very
                         * meaningful errors. E.g., we see lxml failed to install, do you have libxml2
                         * installed? E.g., we see pyOpenSSL>0.15 failed to install, do you have libffi
                         * installed?
                         */
                        println(output.toString().trim())
                        throw new GradleException(
                            "Failed to install ${shortHand}. Please see above output for reason, or re-run your build using ``ligradle -i build`` for additional logging.")
                    } else {
                        if (settings.consoleOutput == ConsoleOutput.RAW) {
                            println output.toString().trim()
                        } else {
                            println("Installed ${shortHand} ".padRight(50, '.') + PythonHelpers.successFlair(project, settings))
                        }
                    }
                }
            }
        }
    }
}
