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
package com.linkedin.gradle.python

import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.util.ConsoleOutput
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Configuration settings for Python products.
 * <p>
 * These values are added as a plugin extension and can be customized in the
 * build.gradle files of clients.
 */
class PythonExtension {

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

    /** The location of this project's Sphinx documentation directory. */
    public String docsDir

    /** The location of this project's tests directory. */
    public String testDir

    /** The location of this project's source directory. */
    public String srcDir

    /** The location of this project's setup.cfg file. */
    public String setupCfg

    /** Container of the details related to the venv/python instance */
    private final PythonDetails details

    public ConsoleOutput consoleOutput = ConsoleOutput.ASCII

    public PythonExtension(Project project) {
        this.details = new PythonDetails(project, new File(project.buildDir, "venv"))
        docsDir = project.file("${project.projectDir}/docs").path
        testDir = project.file("${project.projectDir}/test").path
        srcDir = project.file("${project.projectDir}/src").path
        setupCfg = project.file("${project.projectDir}/setup.cfg").path

        pythonEnvironment = [
                'PATH': project.file("${details.virtualEnv.absolutePath}/bin").path + ':' + System.getenv('PATH'),]

        pythonEnvironmentDistgradle = ['DISTGRADLE_PRODUCT_NAME'   : project.name,
                                       'DISTGRADLE_PRODUCT_VERSION': "${ -> project.version }",]

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

    public PythonDetails getDetails() {
        return details
    }

    public Map<String, Object> getEnvironment() {
        return pythonEnvironment
    }
}


