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
package com.linkedin.gradle.python.util

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import com.linkedin.gradle.python.PythonExtension


class PexFileUtil {

    private PexFileUtil() {
        //private constructor for util class
    }

    /**
     * Build a pex file.
     *
     * @param project The project to run <code>pex</code> within.
     * @param pexCache The directory to use for pex's build cache.
     * @param pexName The name to use for the output pex file.
     * @param repoDir The repository (usually a wheel-cache) to use to build the pex file.
     * @param pexShebang The explicit shebang line to be prepended to the resulting pex file.
     * @param entryPoint The entry point to burn into the pex file, or <code>null</code> if no entry point should be used.
     */
    public static void buildPexFile(Project project, File pexCache, String pexName, File repoDir, String pexShebang, String entryPoint) {
        PythonExtension settings = project.getExtensions().getByType(PythonExtension)
        def arguments = []
        arguments << '--no-pypi'
        arguments << '--cache-dir' << pexCache.absolutePath
        arguments << '--output-file' << pexName
        arguments << '--repo' << repoDir.absolutePath
        arguments << '--python-shebang' << pexShebang
        if (entryPoint) {
            arguments << '--entry-point' << entryPoint
        }
        new ByteArrayOutputStream().withStream { output ->
            ExecResult buildPexResult = project.exec { ExecSpec execSpec ->
                execSpec.environment settings.pythonEnvironment
                execSpec.standardOutput = output
                execSpec.errorOutput = output
                execSpec.ignoreExitValue = true
                execSpec.commandLine([
                    settings.details.getVirtualEnvInterpreter(),
                    settings.details.getVirtualEnvironment().getPex(),
                    *arguments,
                    *pipFreeze(project),
                ])
            }
            if (buildPexResult.exitValue != 0) {
                def outputString = output.toString().trim()
                println(outputString)
                def packageMatcher = (outputString =~ /(?s).*Could not satisfy all requirements for ([\w.-]+):.*/)
                def packageName = "<see output above>"
                if (packageMatcher.matches()) {
                    packageName = packageMatcher[0][1]
                }

                throw new GradleException(
                    """
                    | Failed to build a pex file (see output above)!
                    |
                    | This typically happens because your virtual environment contains a cached copy of ${packageName}
                    | that no other package depends on any more.
                    | Usually, this is the result of updating a package that used to depend on ${packageName}.
                    |
                    | Another possible reason is that you started using a newer version of Python
                    | without checking if all the libraries you use are compatible.
                    | If you can find the missing package in the output above with the message
                    | [EXCLUDED], then it works only with lower versions of Python.
                    """.stripMargin().stripIndent()
                )

            }
        }
    }

    /**
     * Run ``pip freeze`` and return the results.
     *
     * TODO: Make this configurable with other users 'special cases'
     *
     * @param project The project to run ``pip freeze`` within.
     * @return A list of requirements that looks like ['-r', 'requests', '-r', ...].
     */
    @SuppressWarnings("UnusedVariable")
    static List<String> pipFreeze(Project project) {
        PythonExtension settings = project.getExtensions().getByType(PythonExtension)

        // Setup requirements, build, and test dependencies
        Set<String> developmentDependencies = configurationToSet(project.configurations.setupRequires.files)
        developmentDependencies.addAll(configurationToSet(project.configurations.build.files))
        developmentDependencies.addAll(configurationToSet(project.configurations.test.files))
        developmentDependencies.removeAll(configurationToSet(project.configurations.python.files))

        if (settings.details.pythonVersion.pythonMajorMinor == '2.6' && developmentDependencies.contains('argparse')) {
            developmentDependencies.remove('argparse')
        }

        ByteArrayOutputStream requirements = new ByteArrayOutputStream()

        project.exec {
            environment settings.pythonEnvironment
            commandLine([
                settings.details.getVirtualEnvInterpreter(),
                settings.details.getVirtualEnvironment().getPip(),
                'freeze',
                '--disable-pip-version-check',
            ])
            standardOutput requirements
        }

        List<String> reqs = []

        requirements.toString().split(System.getProperty("line.separator")).each {
            List<String> parts = it.split('==')
            String name = parts[0]
            boolean editable = name.startsWith("-e ")
            // The tar name can have _ when package name has -, so check both.
            if (!(editable || developmentDependencies.contains(name)
                || developmentDependencies.contains(name.replace('-', '_')))) {
                reqs.add(name)
            }
        }

        /*
         * Starting with pip-9.x the current project will be editable in freeze.
         * We need to add it unconditionally if it gets skipped above.
         */
        if (!reqs.contains(project.getName())) {
            reqs.add(project.getName())
        }

        return reqs
    }

    /**
     * Convert a collection of files into a set of names.
     *
     * @param files The set of files to add to the set.
     * @return A set of names.
     */
    private static Set<String> configurationToSet(Collection<File> files) {
        Set<String> configNames = new HashSet<String>()
        for (File file : files) {
            def packageInfo = PackageInfo.fromPath(file.name)
            configNames.add(packageInfo.name)
        }
        return configNames
    }

    public static String createThinPexFilename(String name) {
        if (OperatingSystem.current().isWindows()) {
            return name + ".py"
        } else {
            return name + ".pex"
        }
    }

    public static String createFatPexFilename(String name) {
        if (OperatingSystem.current().isWindows()) {
            return name + ".py"
        } else {
            return name
        }
    }
}
