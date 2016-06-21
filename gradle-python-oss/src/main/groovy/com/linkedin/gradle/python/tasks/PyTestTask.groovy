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

import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import groovy.transform.CompileStatic
import org.gradle.api.internal.tasks.options.Option
import org.gradle.process.ExecResult


/**
 * Run py.test on test directory
 */
@CompileStatic
class PyTestTask extends AbstractPythonTestSourceDefaultTask {

    private static final int NO_TESTS_COLLECTED_ERRNO = 5

    boolean specificFileGiven = false

    PyTestTask() {
        args(VirtualEnvExecutableHelper.findExecutable(component, "bin/py.test").absolutePath)
        ignoreExitValue = true
    }

    @Override
    public void preExecution() {
        if (!specificFileGiven) {
            args(component.testDir)
        }
    }

    @Override
    void processResults(ExecResult execResult) {
        if (execResult.exitValue == NO_TESTS_COLLECTED_ERRNO) {
            logger.warn("***** WARNING: You did not write any tests! *****")
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
