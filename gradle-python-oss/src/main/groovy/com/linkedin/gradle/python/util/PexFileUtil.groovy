package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.PythonComponent
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec


class PexFileUtil {

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
        PythonComponent settings = project.getExtensions().getByType(PythonComponent)
        def arguments = []
        arguments << '--no-pypi'
        arguments << '--cache-dir' << pexCache.absolutePath
        arguments << '--output-file' << pexName
        arguments << '--repo' << repoDir.absolutePath
        arguments << '--python-shebang' << pexShebang
        if (entryPoint)
            arguments << '--entry-point' << entryPoint
        new ByteArrayOutputStream().withStream { output ->
            ExecResult buildPexResult = project.exec { ExecSpec execSpec ->
                execSpec.environment settings.pythonEnvironment
                execSpec.standardOutput = output
                execSpec.errorOutput = output
                execSpec.ignoreExitValue = true
                execSpec.commandLine([
                        VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                        VirtualEnvExecutableHelper.getPex(settings),
                        *arguments,
                        *pipFreeze(project),
                ])
            }
            if (buildPexResult.exitValue != 0) {
                def outputString = output.toString().trim()
                println(outputString)
                def packageMatcher = (outputString =~ /(?s).*Could not satisfy all requirements for ([\w\-]+):.*/)
                def packageName = "<see output above>"
                if (packageMatcher.hasGroup())
                    packageName = packageMatcher[0][1]

                def dependencyInsightPath = project.getTasks().getByName('dependencyInsight').getPath()
                throw new GradleException(
                        """\
                    | Failed to build a pex file (see output above)!
                    |
                    | This typically happens because your virtual environment contains a cached copy of ${packageName}
                    | that no other package depends on any more.
                    | Usually, this is the result of updating a package that used to depend on ${packageName}.
                    |
                    | To resolve the issue, simply clean up your working copy completely and rebuild.
                    | See http://go/pygradle-faq "How do I completely clean the checked out product?".
                    |
                    | If that doesn't help, you can analyze your dependencies as described in http://go/pygradle-faq
                    | in questions that contain "print the dependency ..." phrase. Start debugging by running:
                    | \tligradle ${dependencyInsightPath} --dependency ${packageName} --configuration python
                    """.stripMargin().stripIndent()
                )

            }
        }
    }

    /**
     * Run ``pip freeze`` and return the results.
     *
     * @param project The project to run ``pip freeze`` within.
     * @return A list of requirements that looks like ['-r', 'requests', '-r', ...].
     */
    static List<String> pipFreeze(Project project) {
        PythonComponent settings = project.getExtensions().getByType(PythonComponent)

        /** Special cases, such as sphinx-rtd-theme with weird metadata */
        Set<String> specialCases = new HashSet<String>()
        specialCases.addAll(['sphinx-rtd-theme', 'sphinx_rtd_theme'])

        /** Setup requirements, build, and test dependencies + special cases */
        Set<String> developmentDependencies = configurationToSet(project.configurations.setupRequires.files)
        developmentDependencies.addAll(configurationToSet(project.configurations.build.files))
        developmentDependencies.addAll(configurationToSet(project.configurations.test.files))
        developmentDependencies.addAll(specialCases)
        developmentDependencies.removeAll(configurationToSet(project.configurations.python.files))

        if (settings.pythonDetails.pythonVersion.pythonMajorMinor == '2.6' && developmentDependencies.contains('argparse')) {
            developmentDependencies.remove('argparse')
        }

        ByteArrayOutputStream requirements = new ByteArrayOutputStream()

        project.exec {
            environment settings.pythonEnvironment
            commandLine([
                    VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                    VirtualEnvExecutableHelper.getPip(settings),
                    'freeze',
                    '--disable-pip-version-check',
            ])
            standardOutput requirements
        }

        List<String> reqs = []

        requirements.toString().split('\n').each {
            def (String name, String version) = it.split('==')
            if (!developmentDependencies.contains(name)) {
                reqs.add(name)
            }
        }

        return reqs
    }

    /**
     * Convert a collection of files into a set of names.
     *
     * @param files The set of files to add to the set.
     * @return A set of names.
     */
    protected static Set<String> configurationToSet(Collection<File> files) {
        Set<String> configNames = new HashSet<String>()
        for (File file : files) {
            def (String name, String version) = MiscUtils.packageInfoFromPath(file.name)
            configNames.add(name)
        }
        return configNames
    }

}
