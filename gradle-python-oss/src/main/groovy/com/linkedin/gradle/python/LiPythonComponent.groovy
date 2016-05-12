package com.linkedin.gradle.python


import com.linkedin.gradle.python.plugin.PythonPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.ExecSpec


/**
 * Configuration settings for Python products.
 * <p>
 * These values are added as a plugin extension and can be customized in the
 * build.gradle files of clients.
 */
class LiPythonComponent {

    /** The environment to use for all Python commands. */
    public Map<String, Object> pythonEnvironment

    /**
     * The environment to use for Python commands run on the project being
     * developed.
     * <p>
     * This environment only applies to the project being developed. In other
     * words, this environment will only be passed to commands that use a
     * <code>setup.py</code> file.
     */
    public Map<String, Object> pythonEnvironmentDistgradle

    /** The desired destination of the <code>activate</code> link. */
    public String activateLinkDest

    /**
     * The desired destination of the <code>config</code> link.
     * <p>
     * This link refers to the Config 2.0 that a project has, if applicable.
     */
    public String configLinkDest

    /** The location of the pex runnable. */
    public String pexLocation

    /**The location of the pip runnable. */
    public String pipLocation

    /** The location of the py.test runnable. */
    public String pytestLocation

    /** The location of the sphinx runnable. */
    public String sphinxLocation

    /** The location of the flake8 runnable. */
    public String flake8Location

    /**
     * The desired destination of the <code>product-spec.json</code> link.
     * <p>
     * Due to packaging limitation with Python 2.6, this file will exist as a
     * hard link.
     */
    public String productSpecLinkDest

    /** The location of the python runnable. */
    public String pythonLocation

    /** The location of the system's python runnable. */
    public String systemPython

    /** The desired location of the virtual environment. */
    public String virtualenvLocation

    /** The desired prompt for the virtual environment. */
    public String virtualenvPrompt

    /** The location of this project's Sphinx documentation directory. */
    public String docsDir

    /** The location of this project's tests directory. */
    public String testDir

    /** The location of this project's source directory. */
    public String srcDir

    /** The location of this project's setup.cfg file. */
    public String setupCfg

    /** The exact version of Python this project uses, such as '2.7.11'. */
    public String pythonVersion

    /** The short version of Python this project uses, such as '2.7'. */
    public String pythonMajorMinor

    /** The directory used to structure a deployable entity */
    public String deployableBuildDir

    /** The directory used to structure a deployable entity's bin directory */
    public String deployableBinDir

    /** The directory used to structure a deployable entity's etc directory */
    public String deployableEtcDir

    /** The directory to use a pex's build cache */
    public String pexCache

    /** The python interpreter to use at runtime */
    @Deprecated
    public String pexShebang

    /** The directory to use to cache built wheels */
    public String wheelCache

    /** If <code>true</code>, then install skinny pex files. */
    public boolean fatPex

    /** If <code>true</code>, then use Python entry point wrapper. */
    public boolean pythonWrapper

    /** If <code>true</code>, then generate shell completion scripts. */
    public boolean generateCompletions

    /** If <code>true</code>, then always use ASCII when printing to standard out. */
    public boolean asciiOutput

    /** If <code>true</code>, then allow color when printing to standard out. */
    public boolean colorOutput

    /** If <code>true</code>, then do not post-process output from Python commands. */
    public boolean rawOutput

    /** Version of the Python interpreter to use. */
    String interpreterVersion

    /**
     * Custom setter for <code>interpreterVersion</code>.
     * <p>
     * It updates other Python version properties
     * when <code>interpreterVersion</code> is written.
     *
     * @param version  interpreter version to use for builds
     */
    void setInterpreterVersion(String version) {
        interpreterVersion = version
        updatePythonVersionProperties(currentProject)
    }

    /** Path to the Python interpreter used. */
    String interpreterPath

    /**
     * Custom getter for <code>interpreterPath</code>.
     * <p>
     * It uses <code>interpreterVersion</code> and
     * <code>systemPython</code> to determine the path.
     * It tries to use the cleanpython path if available,
     * but falls back to the system Python.
     *
     * @return path to the used interpreter
     */
    String getInterpreterPath() {
        if (interpreterVersion == null) {
            return systemPython
        }

        // Ah, the beauty of Java regex backslashitis.
        String[] version_parts = interpreterVersion.split('\\.')
        Integer major = version_parts[0].toInteger()
        Integer minor = (major == 2) ? 6 : 5
        if (version_parts.length > 1) {
            minor = version_parts[1].toInteger()
        }

        def supportedVersions = [[2, 6], [2, 7], [3, 5]]
        if (!([major, minor] in supportedVersions)) {
            return systemPython
        }

        String version = [major, minor].join('.')

        /*
         * Backward compatibility.
         * Allow a couple of current products that have systemPython set to continue working.
         * TODO: Remove once these products are migrated to use interpreterVersion instead.
         */
        if (systemPython[-3..-1] > version) {
            return systemPython
        }

        File cleanPythonPath = new File("/export/apps/python/${version}/bin/python${version}")
        File systemPythonPath = new File("/usr/bin/python${version}")

        if (cleanPythonPath.exists()) {
            return cleanPythonPath.path
        }

        if (systemPythonPath.exists()) {
            return systemPythonPath.path
        }

        return systemPython
    }

    /**
     * Custom setter for <code>interpreterPath</code>.
     * <p>
     * It throws an exception preventing writing into the property
     * from <code>build.gradle</code>.
     *
     * @param path  an <b>ignored</b> interpreter path
     */
    void setInterpreterPath(String path) {
        throw new GradleException("The interpreterPath is a non-writable property.")
    }


    @Deprecated
    public boolean nextGenCfg2

    /**
     * If <code>NEXT_GEN</code> then generate app-defs using the next-gen
     * mechanism.  If <code>OLD_GEN</code> then use the old-style mechanism.
     * <p>
     * More information can be found in the {@link LiPythonAppDef} plugin.
     */
    public Cfg2Generation cfg2

    /*
     * NOTE: Maintainers, please leave the following undocumented!
     * We don't want people changing these from under us, because
     * it could break the greater build system.
     */
    public String activateLinkSource
    public String configLinkSource
    public String productSpecLinkSource
    public String virtualenvPackageDir
    public String virtualenvPackageScript
    public String virtualenvPackageVersion

    // Reference to the project these settings are initialized for.
    private final Project currentProject

    LiPythonComponent(Project project) {
        currentProject = project
        virtualenvPackageVersion = PythonPlugin.PINNED_VERSIONS['virtualenv'].version
        virtualenvPackageDir = project.file("${project.rootProject.buildDir}/${project.name}/vendor").path
        virtualenvPackageScript = project.file("${virtualenvPackageDir}/virtualenv-${virtualenvPackageVersion}/virtualenv.py").path
        systemPython = project.file("/usr/bin/python2.6").path
        virtualenvLocation = project.file("${project.buildDir}/venv").path
        pythonLocation = project.file("${virtualenvLocation}/bin/python").path
        pipLocation = project.file("${virtualenvLocation}/bin/pip").path
        pexLocation = project.file("${virtualenvLocation}/bin/pex").path
        pytestLocation = project.file("${virtualenvLocation}/bin/py.test").path
        flake8Location = project.file("${virtualenvLocation}/bin/flake8").path
        sphinxLocation = project.file("${virtualenvLocation}/bin/sphinx-build").path
        activateLinkSource = project.file("${virtualenvLocation}/bin/activate").path
        activateLinkDest = project.file("${project.projectDir}/activate").path
        productSpecLinkSource = project.file("${project.rootProject.projectDir}/product-spec.json").path
        productSpecLinkDest = project.file("${project.projectDir}/product-spec.json").path
        configLinkSource = project.file("${project.rootProject.projectDir}/config").path
        configLinkDest = project.file("${project.projectDir}/config").path
        virtualenvPrompt = "(${project.name})"
        docsDir = project.file("${project.projectDir}/docs").path
        testDir = project.file("${project.projectDir}/test").path
        srcDir = project.file("${project.projectDir}/src").path
        setupCfg = project.file("${project.projectDir}/setup.cfg").path
        fatPex = false
        pythonWrapper = true
        generateCompletions = false
        nextGenCfg2 = false
        cfg2 = Cfg2Generation.OLD_GEN
        deployableBuildDir = project.file("${project.rootProject.buildDir}/${project.name}/deployable").path
        deployableBinDir = project.file("${deployableBuildDir}/bin").path
        deployableEtcDir = project.file("${deployableBuildDir}/etc").path
        pexCache = project.file("${project.rootProject.buildDir}/${project.name}/pex").path
        pexShebang = systemPython
        wheelCache = project.file("${project.rootProject.buildDir}/${project.name}/wheel-cache").path
        asciiOutput = true
        colorOutput = false
        rawOutput = false
        interpreterVersion = null

        /*
         * Include the current version of OpenSSL on SysOps hosts.
         * It's ok if this doesn't exist for other hosts.
         * The "-Wl," tells gcc to pass the next onto the linker,
         * which sets the rpath to burn the library path into the .so
         *
         * NOTE: The C/LDFLAGS described above are not used any more.
         * This option must not be used on Mac OS X.
         * It caused other issues and was commented out until further review.
         */
        pythonEnvironment = [
                // 'CFLAGS': '-I/export/apps/openssl/include',
                // 'LDFLAGS': '-Wl,-rpath,/export/apps/openssl/lib -L/export/apps/openssl/lib',
                'PATH': project.file("${virtualenvLocation}/bin").path + ':' + System.getenv('PATH'),
        ]

        pythonEnvironmentDistgradle = [
                'DISTGRADLE_PRODUCT_NAME': project.name,
                'DISTGRADLE_PRODUCT_VERSION': "${ -> project.version }",
        ]

        updatePythonVersionProperties(project)

        /*
         * NOTE: Do lots of sanity checking and validation here.
         * We want to be nice to our users and tell them if things are missing
         * or mis-configured as soon as possible.
         */
        if (pythonEnvironment.containsKey('DISTGRADLE_PRODUCT_NAME'))
            throw new GradleException("Cannot proceed with `DISTGRADLE_PRODUCT_NAME` set in environment!")

        if (pythonEnvironment.containsKey('DISTGRADLE_PRODUCT_VERSION'))
            throw new GradleException("Cannot proceed with `DISTGRADLE_PRODUCT_VERSION` set in environment!")

    }

    /**
     * Should next-gen cfg2 app-defs be used.
     *
     * @return true if next-gen cfg2 app-defs should be used, false otherwise
     */
    public boolean useNextGenCfg2() {
        return nextGenCfg2 || cfg2 == Cfg2Generation.NEXT_GEN
    }

    /*
     * Get the exact version of Python at the interpreter path.
     *
     * @param project  the project running this method
     * @return         the Python version, such as 2.6.9, 2.7.11, or 3.5.1
     */
    private String getPythonVersion(Project project) {
        new ByteArrayOutputStream().withStream { output ->
            project.exec { ExecSpec execSpec ->
                execSpec.executable = interpreterPath
                execSpec.args = ['--version']
                execSpec.standardOutput = output
                execSpec.errorOutput = output
                execSpec.ignoreExitValue = true
            }
            return output.toString().trim().tokenize(' ')[1]
        }
    }

    /*
     * Update Python version properties from the interpreter path.
     *
     * @param project  the project running this method
     */
    private void updatePythonVersionProperties(Project project) {
        // Assigning to project.ext allows the use of just 'pythonVersion' in build.gradle files.
        pythonVersion = project.ext.pythonVersion = getPythonVersion(project)
        pythonMajorMinor = pythonVersion.tokenize('.')[0..1].join('.')
    }

}


