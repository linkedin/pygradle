package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.LiPythonComponent
import groovy.transform.Memoized
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher

abstract class PythonHelpers {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Detect if we're connected to a TTY and support color.
     * <p>
     * @param project The project to use to invoke the command.
     * @return true if we're connected to a TTY and support color.
     */
    @Memoized
    protected static boolean isTty(Project project) {

        if ((project.pythonSettings as LiPythonComponent).asciiOutput)
            return false

        def output = new ByteArrayOutputStream()

        ExecResult result = project.exec { ExecSpec execSpec ->
            execSpec.commandLine(['tput', 'colors'])
            execSpec.standardOutput = output
            execSpec.ignoreExitValue = true
        }

        if (result.exitValue != 0) {
            project.logger.info('Failed to detect if tty with tput!')
            return false
        }

        return (output.toString().trim() as int) >= 8
    }

    /**
     * Return a string that indicates success!
     * <p>
     * If not connected to a TTY that supports color, then an ASCII success
     * message will be returned.
     * <p>
     * @param project The project running this method.
     * @return A string indicating success!
     */
    public static String successFlair(Project project) {

        LiPythonComponent settings = project.pythonSettings

        StringBuilder successFlair = new StringBuilder()

        if (settings.colorOutput)
            successFlair << ANSI_GREEN

        if (isTty(project))
            successFlair << "\u2713"
        else
            successFlair << ' [GOOD]'

        if (settings.colorOutput)
            successFlair << ANSI_RESET

        return successFlair.toString()
    }

    /**
     * Returns operating system and architecture pair.
     * <p>
     * The operating system and architecture pair is joined with an "_"
     * character in compliance with the multi-variant build system. Read more
     * about the multi-variant build system at http://go/mbf.
     * @return The operating system and architecture pair.
     */
    protected static String getOsNameAndArch() {
        def osName = System.getProperty('os.name').toLowerCase()
        if (osName == 'mac os x')
            osName = 'darwin'
        def osArch = System.getProperty('os.arch')
        if (osArch == 'amd64') {
            osArch = 'x86_64'
        }
        return "${osName}_${osArch}";
    }

    /**
     * Returns a multi-variant product name.
     * <p>
     * The multi-variant product name is a valid product <i>name</i> per the
     * multi-variant build system. Read more about the multi-variant build
     * system at http://go/mbf.
     * @param product The product name.
     * @return The full multi-variant product name.
     */
    protected static String getVariantName(String product) {
        return "${product}_${getOsNameAndArch()}"
    }

    /**
     * Derives a Python package's name and version from its path.
     * <p>
     * This method only recognizes packages with the following extensions. A
     * Python package that doesn't have one of the following exceptions will
     * raise an exception.
     * <ul>
     *   <li>.tar.gz</li>
     *   <li>.tar.bz2</li>
     *   <li>.tar</li>
     *   <li>.tgz</li>
     *   <li>.zip</li>
     * </ul>
     * <p>
     * A path to a expanded Python package can be provided as long as the path
     * to the expanded Python package refers to the directory that contains the
     * Python package.
     * <p>
     * This method only recognizes Python packages that follow the convention
     * <pre>(name)-(version)[-(extra)].(extension)</pre>. The <pre>extra</pre>
     * field may include something like <pre>-SNAPSHOT</pre> or
     * <pre>-linkedin1</pre>. A Python package that doesn't follow this
     * convention will raise an exception.
     * <p>
     * @param packagePath The path to a Python package.
     */
    public static Collection<String> packageInfoFromPath(String packagePath) {
        def extensionRegex = /\.tar\.gz|\.zip|\.tar|\.tar\.bz2|\.tgz/
        def nameVersionRegex = /^((.*)\/)*(?<name>[a-zA-Z0-9._\-]+)-(?<version>([0-9][0-9a-z\.]+(-.*)*))$/

        def packageName = packagePath.split(extensionRegex).first()

        if (new File(packagePath).isDirectory())
            return [packagePath.split(File.separator)[-1], null]

        if (packagePath == packageName)
            throw new GradleException("Cannot calculate Python package extension from ${packagePath} using regular expression /${extensionRegex}/.")

        Matcher matcher = packageName =~ nameVersionRegex
        if (matcher.matches()) {
            def name = matcher.group('name')
            def version = matcher.group('version')
            return [name, version]
        } else {
            throw new GradleException("Cannot calculate Python package name and version from ${packageName} using regular expression /${nameVersionRegex}/.")
        }
    }

    /**
     * Check if a dependency is in a collection of files.
     *
     * @param dependency The dependency to test.
     * @param files The set of files to test against.
     * @return True if dependency is in set of files.
     */
    protected static boolean inConfiguration(String dependency, Collection<File> files) {
        for (File file : files) {
            def (name, version) = packageInfoFromPath(file.name)
            if (dependency == name)
                return true
        }
        false
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
            def (String name, String version) = packageInfoFromPath(file.name)
            configNames.add(name)
        }
        return configNames
    }

    protected static boolean transitiveBuildTest(String name, Project project) {
        return (
          name in ['sphinx-rtd-theme', 'sphinx_rtd_theme'] &&
          !inConfiguration(name, project.configurations.python.files)
        )
    }

    /**
     * Run ``pip freeze`` and return the results.
     *
     * @param project The project to run ``pip freeze`` within.
     * @return A list of requirements that looks like ['-r', 'requests', '-r', ...].
     */
    protected List<String> pipFreeze(Project project) {
        LiPythonComponent settings = project.pythonSettings

        /** Special cases, such as sphinx-rtd-theme with weird metadata */
        Set<String> specialCases = new HashSet<String>()
        specialCases.addAll(['sphinx-rtd-theme', 'sphinx_rtd_theme'])

        /** Setup requirements, build, and test dependencies + special cases */
        Set<String> developmentDependencies = configurationToSet(project.configurations.setupRequires.files)
        developmentDependencies.addAll(configurationToSet(project.configurations.build.files))
        developmentDependencies.addAll(configurationToSet(project.configurations.test.files))
        developmentDependencies.addAll(specialCases)
        developmentDependencies.removeAll(configurationToSet(project.configurations.python.files))

        if (settings.pythonMajorMinor == '2.6' && developmentDependencies.contains('argparse')) {
            developmentDependencies.remove('argparse')
        }

        ByteArrayOutputStream requirements = new ByteArrayOutputStream()

        project.exec {
            environment settings.pythonEnvironment
            commandLine([
                    settings.pythonLocation,
                    settings.pipLocation,
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
     * Run ``setup.py entrypoints`` and return the results.
     *
     * The ``entrypoints`` setuptools command is a custom command provided by the distgradle.GradleDistribution
     * distribution class. Only classes that use this distribution class support this command.
     *
     * @param project The project to run ``setup.py entrypoints`` within.
     * @return A list of setuptools entry point strings that looks like ['foo = module1.module2:main', ...].
     */
    protected List<String> collectEntryPoints(Project project) {
        LiPythonComponent settings = project.pythonSettings
        def entryPointsBuf = new ByteArrayOutputStream()
        project.exec {
            environment settings.pythonEnvironment + settings.pythonEnvironmentDistgradle
            commandLine([
                    project.pythonSettings.pythonLocation,
                    'setup.py',
                    'entrypoints',
            ])
            standardOutput entryPointsBuf
        }
        def entryPoints = []
        entryPointsBuf.toString().split('\n').each {
            if (it != 'running entrypoints')
                entryPoints << it
        }
        return entryPoints
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
    protected void buildPexFile(Project project, String pexCache, String pexName, String repoDir, String pexShebang, String entryPoint) {
        LiPythonComponent settings = project.pythonSettings
        def arguments = []
        arguments << '--no-pypi'
        arguments << '--cache-dir' << pexCache
        arguments << '--output-file' << pexName
        arguments << '--repo' << repoDir
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
                    settings.pythonLocation,
                    settings.pexLocation,
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
     * Write an thin pex entry point script.
     * <p>
     * An entry point script is also referred to as a wrapper script. This script simply wraps instruments a
     * call to pex with an entry point.
     * <p>
     * An entry point script includes LID specific environment variables. For example, the <code>BASEDIR</code>
     * is used to calculate the base directory at which to unpack the pex.
     * @param project The project to run <code>pex</code> within.
     * @param path The path at which to create the wrapper script.
     * @param entryPoint The entry point to use in the wrapper script.
     */
    protected static void writeEntryPointScript(Project project, String path, String entryPoint) {
        LiPythonComponent settings = project.pythonSettings
        boolean isCliTool = project.hasProperty('isCliTool') ? project.property('isCliTool') : false
        def file = new File(path)
        if (file.exists())
            file.delete()
        file.createNewFile()
        if (isCliTool) {
            file.setExecutable(true, false)
            file.setReadable(true, false)
        } else {
            file.setExecutable(true)
        }
        if (isCliTool && settings.pythonWrapper) {
            def pythonWrapperTemplate = PythonPexDistributionPlugin.getResource('/templates/pex_entrypoint.py.template').text
            file << String.format(pythonWrapperTemplate, "${project.name}.pex", entryPoint)
        } else {
            file << """#!/bin/bash\n"""
            file << """[ -z "\$BASEDIR" ] && BASEDIR=\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )/.." && pwd )\n"""
            file << """exec /usr/bin/env PEX_ROOT="\$BASEDIR/libexec" PEX_MODULE="${entryPoint}" """
            file << """\$BASEDIR/bin/${project.name}.pex "\$@"\n"""
        }
    }

    /**
     * Make a link
     * <p>
     * Make a link using the system's ``ln`` command.
     * <p>
     * @param project The project to run within.
     * @param target The target directory that the link points to.
     * @param destination The destination directory or the name of the link.
     * @param symlink Whether to create a link or symlink.
     */
    protected static void makeLink(Project project, String target, String destination, boolean symlink) {
        /*
         * Check if the file exists because the link checking logic in Gradle differs
         * between Linux and OS X machines.
         */
        if (!project.file(destination).exists()) {

            if (symlink) {
                Files.createSymbolicLink(Paths.get(destination), Paths.get(target))
            } else {
                Files.createLink(Paths.get(destination), Paths.get(target))
            }
        }
    }

    /**
     * Compare two versions
     * <p>
     * Compare versionA with versionB.
     * <p>
     * @param versionA The String value of version A.
     * @param versionB The String value of version B.
     * @return An Integer value indicating whether versionA is greater than, equal to or less than versionB.
     */
     protected static int compareVersions(String versionA, String versionB) {
        List verA = versionA.tokenize('.-')
        List verB = versionB.tokenize('.-')

        int commonIndices = Math.min(verA.size(), verB.size())

        for (int i = 0; i < commonIndices; ++i) {
            int numA = verA[i].toInteger()
            int numB = verB[i].toInteger()
            if (numA != numB) {
                return numA > numB ? 1 : -1
            }
        }

        if (verA.size() == verB.size()) {
            return 0
        } else {
            return verA.size() > verB.size() ? 1 : -1
        }
     }

}

