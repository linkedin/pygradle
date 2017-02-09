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
import com.linkedin.gradle.python.extension.VirtualEnvironment
import com.linkedin.gradle.python.util.ConsoleOutput
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.nio.file.Paths

/**
 * Configuration settings for Python products.
 * <p>
 * These values are added as a plugin extension and can be customized in the
 * build.gradle files of clients.
 */
class PythonExtension {

    /** The environment to use for all Python commands. */
    public Map<String, String> pythonEnvironment

    /**
     * The environment to use for Python commands run on the project being
     * developed.
     * <p>
     * This environment only applies to the project being developed. In other
     * words, this environment will only be passed to commands that use a
     * <code>setup.py</code> file.
     */
    public Map<String, String> pythonEnvironmentDistgradle

    /** The location of this project's Sphinx documentation directory. */
    public String docsDir

    /** The location of this project's tests directory. */
    public String testDir

    /** The location of this project's source directory. */
    public String srcDir

    /** The location of this project's setup.cfg file. */
    public String setupCfg

    /** The name of the pinned requirements file. */
    public File pinnedFile

    /** A way to define forced versions of libraries */
    public Map<String, Map<String, String>> forcedVersions = [
        'appdirs'       : ['group': 'pypi', 'name': 'appdirs', 'version': '1.4.0'],
        'argparse'      : ['group': 'pypi', 'name': 'argparse', 'version': '1.4.0'],
        'flake8'        : ['group': 'pypi', 'name': 'flake8', 'version': '2.5.4'],
        'packaging'     : ['group': 'pypi', 'name': 'packaging', 'version': '16.8'],
        'pbr'           : ['group': 'pypi', 'name': 'pbr', 'version': '1.8.0'],
        'pex'           : ['group': 'pypi', 'name': 'pex', 'version': '1.1.4'],
        'pip'           : ['group': 'pypi', 'name': 'pip', 'version': '7.1.2'],
        'pytest'        : ['group': 'pypi', 'name': 'pytest', 'version': '2.9.1'],
        'pytest-cov'    : ['group': 'pypi', 'name': 'pytest-cov', 'version': '2.2.1'],
        'pytest-xdist'  : ['group': 'pypi', 'name': 'pytest-xdist', 'version': '1.14'],
        'setuptools'    : ['group': 'pypi', 'name': 'setuptools', 'version': '19.1.1'],
        'setuptools-git': ['group': 'pypi', 'name': 'setuptools-git', 'version': '1.1'],
        'six'           : ['group': 'pypi', 'name': 'six', 'version': '1.10.0'],
        'Sphinx'        : ['group': 'pypi', 'name': 'Sphinx', 'version': '1.4.1'],
        'virtualenv'    : ['group': 'pypi', 'name': 'virtualenv', 'version': '15.0.1'],
        'wheel'         : ['group': 'pypi', 'name': 'wheel', 'version': '0.26.0'],
    ]

    /* Container of the details related to the venv/python instance */
    private final PythonDetails details

    public ConsoleOutput consoleOutput = ConsoleOutput.ASCII

    PythonExtension(Project project) {
        this.details = new PythonDetails(project)
        docsDir = Paths.get(project.projectDir.absolutePath, "docs").toFile().path
        testDir = Paths.get(project.projectDir.absolutePath, "test").toFile().path
        srcDir = Paths.get(project.projectDir.absolutePath, "src").toFile().path
        setupCfg = Paths.get(project.projectDir.absolutePath, "setup.cfg").toFile().path

        // creating a flake8 config file if one doesn't exist, this prevents "file not found" issues.
        def cfgCheck = project.file(setupCfg)
        if (!cfgCheck.exists()){
            project.logger.lifecycle("Flake8 config file doesn't exist, creating default")
            cfgCheck.createNewFile()
            cfgCheck << "[flake8]"
        } else {
            project.logger.lifecycle("Flake8 config file exists")
        }

        pinnedFile = project.file("pinned.txt")

        def applicationDirectory = VirtualEnvironment.getPythonApplicationDirectory()

        pythonEnvironment = [
                'PATH': "${ -> details.virtualEnv.toPath().resolve(applicationDirectory).toAbsolutePath().toString() }" + File.pathSeparator + System.getenv('PATH'),]

        pythonEnvironmentDistgradle = ['PYGRADLE_PROJECT_NAME'   : project.name,
                                       'PYGRADLE_PROJECT_VERSION': "${ -> project.version }",]

        /*
         * NOTE: Do lots of sanity checking and validation here.
         * We want to be nice to our users and tell them if things are missing
         * or mis-configured as soon as possible.
         */
        if (pythonEnvironment.containsKey('PYGRADLE_PROJECT_NAME')) {
            throw new GradleException("Cannot proceed with `PYGRADLE_PROJECT_NAME` set in environment!")
        }

        if (pythonEnvironment.containsKey('PYGRADLE_PROJECT_VERSION')) {
            throw new GradleException("Cannot proceed with `PYGRADLE_PROJECT_VERSION` set in environment!")
        }
    }

    void forceVersion(String group, String name, String version) {
        Objects.requireNonNull(group, "Group cannot be null")
        Objects.requireNonNull(name, "Name cannot be null")
        Objects.requireNonNull(version, "Version cannot be null")
        forcedVersions[name] = ['group': group, 'name': name, 'version': version]
    }

    void forceVersion(String gav) {
        Objects.requireNonNull(gav, "GAV cannot be null")

        String[] split = gav.split(':')
        if (split.length < 3) {
            throw new GradleException("Unable to parse GAV $gav")
        }

        forceVersion(split[0], split[1], split[2])
    }

    PythonDetails getDetails() {
        return details
    }

    /**
     * Configures the {@link PythonDetails} for the project.
     *
     * @param a {@link Closure} that will delegate to {@link PythonDetails}
     */
    void details(@DelegatesTo(PythonDetails) Closure cl) {
        cl.delegate = details
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    Map<String, Object> getEnvironment() {
        return pythonEnvironment
    }

    File getPinnedFile() {
        return pinnedFile
    }

    void setPinnedFile(File pinnedFile) {
        this.pinnedFile = pinnedFile
    }
}


