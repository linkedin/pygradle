package com.linkedin.gradle.python.tasks


import groovy.transform.CompileStatic
import org.gradle.api.internal.tasks.options.Option
import org.gradle.process.ExecResult


/**
 * Run py.test on test directory
 */
@CompileStatic
class PyTestTask extends AbstractPythonTestSourceDefaultTask {

    boolean specificFileGiven = false;

    PyTestTask() {
        args("${component.pytestLocation}")
    }

    @Override
    public void preExecution() {
        if (!specificFileGiven) {
            args(component.testDir)
        }
    }

    @Override
    void processResults(ExecResult execResult) {
    }

    /**
     * Run tests on single file
     *
     * @param file to run tests on
     */
    @Option(option = "file", description = "Only run tests on the input file")
    public void filterFiles(String file) {
        specificFileGiven = true;
        args(file)
    }

    /**
     * Only test one file
     *
     * @param testName name of the tests to be executed
     */
    @Option(option = "test-name", description = "Only run tests matching description")
    public void filterTestCase(String testName) {
        args('-k', testName)
    }

    /**
     * Enable console logging / debugging
     *
     * @param enabled
     */
    @Option(option = "enable-console", description = "Enables console output")
    public void enableConsole(boolean enabled) {
        if (enabled) {
            args('-s')
        }
    }
}
