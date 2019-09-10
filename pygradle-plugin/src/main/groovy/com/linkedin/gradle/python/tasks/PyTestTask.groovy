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

import groovy.transform.CompileStatic
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecResult

/**
 * Run py.test on test directory
 */
@CompileStatic
class PyTestTask extends AbstractPythonTestSourceDefaultTask {

    private static final int NO_TESTS_COLLECTED_ERRNO = 5
    private static final String NO_TEST_WARNING = "***** WARNING: You did not write any tests! *****"

    // specific test file was given and only the tests in that file should be executed
    private boolean specificFileGiven = false

    PyTestTask() {
        ignoreExitValue = true
    }

    @Override
    public void preExecution() {
        // any arguments to pytest(-k, -s etc..) must go into subArgs to get appended after py.test
        args(pythonDetails.virtualEnvironment.findExecutable("py.test").absolutePath)
        if (!specificFileGiven) {
            args(component.testDir)
        }
    }

    @Override
    void processResults(ExecResult execResult) {
        if (execResult.exitValue == NO_TESTS_COLLECTED_ERRNO) {
            logger.warn(NO_TEST_WARNING)
        } else {
            execResult.assertNormalExitValue()
        }
    }

    /**
     * Run tests on single file
     *
     * @param file to run tests on
     */
    @Option(option = "file", description = "Only run tests on the input file")
    public void filterFiles(String file) {
        specificFileGiven = true
        subArgs(file)
    }

    /**
     * Only run tests matching given substring expression
     *
     * @param testSubstringExpression name of the tests to be executed
     */
    @Option(option = "test-substring", description = "Only run tests matching the given substring")
    public void filterTestCase(String testSubStringExpression) {
        subArgs('-k', testSubStringExpression)
    }

    /**
     * Enable console logging / debugging
     *
     * @param enabled
     */
    @Option(option = "enable-console", description = "Enables console output")
    public void enableConsole(boolean enabled) {
        if (enabled) {
            subArgs('-s')
        }
    }

    /**
     * Only run tests matching given mark expression
     *
     * @param markExpression
     */
    @Option(option = "mark-expression", description = "only run tests matching given mark expression")
    public void filterMarker(String markExpression) {
        subArgs('-m', markExpression)
    }
}
