package com.linkedin.gradle.python

import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.util.ConsoleOutput
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

/**
 * Configuration settings for Python products.
 * <p>
 * These values are added as a plugin extension and can be customized in the
 * build.gradle files of clients.
 */
class PythonComponent {

    /** The environment to use for all Python commands. */
    public Map<String, Object> pythonEnvironment

    /**
     * The environment to use for Python commands run on the project being
     * developed.
     * <p>
     * This environment only applies to the project being developed. In other
     * words, this environment will only be passed to commands that use a
     * <code>setup.py</code> file.*/
    public Map<String, Object> pythonEnvironmentDistgradle

    /** The location of this project's Sphinx documentation directory. */
    public String docsDir

    /** The location of this project's tests directory. */
    public String testDir

    /** The location of this project's source directory. */
    public String srcDir

    /** The location of this project's setup.cfg file. */
    public String setupCfg


    private final PythonDetails pythonDetails

    public ConsoleOutput consoleOutput = ConsoleOutput.RAW

    public PythonComponent(Project project) {
        this.pythonDetails = new PythonDetails(project, new File(project.buildDir, "venv"))
        docsDir = project.file("${project.projectDir}/docs").path
        testDir = project.file("${project.projectDir}/test").path
        srcDir = project.file("${project.projectDir}/src").path
        setupCfg = project.file("${project.projectDir}/setup.cfg").path

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
            'PATH': project.file("${pythonDetails.virtualEnv.absolutePath}/bin").path + ':' + System.getenv('PATH'),]

        pythonEnvironmentDistgradle = ['DISTGRADLE_PRODUCT_NAME'   : project.name,
                                       'DISTGRADLE_PRODUCT_VERSION': "${-> project.version}",]

        /*
         * NOTE: Do lots of sanity checking and validation here.
         * We want to be nice to our users and tell them if things are missing
         * or mis-configured as soon as possible.
         */
        if (pythonEnvironment.containsKey('DISTGRADLE_PRODUCT_NAME')) {
            throw new GradleException("Cannot proceed with `DISTGRADLE_PRODUCT_NAME` set in environment!")
        }

        if (pythonEnvironment.containsKey('DISTGRADLE_PRODUCT_VERSION')) {
            throw new GradleException("Cannot proceed with `DISTGRADLE_PRODUCT_VERSION` set in environment!")
        }
    }

    public PythonDetails getPythonDetails() {
        return pythonDetails
    }

    public Map<String, Object> getEnvironment() {
        return pythonEnvironment
    }
}


