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

import com.linkedin.gradle.python.extension.PexExtension
import com.linkedin.gradle.python.extension.PythonDetails
import com.linkedin.gradle.python.extension.PythonDetailsFactory
import com.linkedin.gradle.python.util.ApplicationContainer
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

    /** Settings that can be put into the pip.conf file in the venv */
    public Map<String, Map<String, String>> pipConfig = [:]

    /** A way to define forced versions of libraries */
    public Map<String, Map<String, String>> forcedVersions = [
        'flake8'        : ['group': 'pypi', 'name': 'flake8', 'version': '3.6.0'],
        'pex'           : ['group': 'pypi', 'name': 'pex', 'version': '1.5.2'],
        'pip'           : ['group': 'pypi', 'name': 'pip', 'version': '18.1'],
        'pytest'        : ['group': 'pypi', 'name': 'pytest', 'version': '3.10.0'],
        'pytest-cov'    : ['group': 'pypi', 'name': 'pytest-cov', 'version': '2.6.0'],
        'pytest-xdist'  : ['group': 'pypi', 'name': 'pytest-xdist', 'version': '1.24.0'],
        'setuptools'    : ['group': 'pypi', 'name': 'setuptools', 'version': '40.5.0'],
        'setuptools-git': ['group': 'pypi', 'name': 'setuptools-git', 'version': '1.2'],
        'six'           : ['group': 'pypi', 'name': 'six', 'version': '1.11.0'],
        'Sphinx'        : ['group': 'pypi', 'name': 'Sphinx', 'version': '1.8.1'],
        'virtualenv'    : ['group': 'pypi', 'name': 'virtualenv', 'version': '16.1.0'],
        'wheel'         : ['group': 'pypi', 'name': 'wheel', 'version': '0.31.1'],
    ]

    /* Container of the details related to the venv/python instance */
    private final PythonDetails details

    /*
     * "Application container" defines the format for bundling the application
     * into a single file distribution.  Examples include pex, shiv, and xar.
     * Not all plugins using this extension support such containers, but for
     * u/i purposes, it's convenient to add this here.  This allows the
     * following in a build.gradle file:
     *
     * python {
     *     container = "shiv"
     * }
     *
     * These will simply be ignored in extension clients that don't need it.
     *
     * Downstream consumers can extend the map between container short names
     * appropriate for the build.gradle UI, and the container class
     * this maps to.  They can also set the default container, which allows
     * them e.g. to choose shivs over pexes.
     */
    public Map<String, ApplicationContainer> containers
    String container
    ApplicationContainer defaultContainer

    PythonExtension(Project project) {
        this.details = PythonDetailsFactory.makePythonDetails(project, null)
        docsDir = Paths.get(project.projectDir.absolutePath, "docs").toFile().path
        testDir = Paths.get(project.projectDir.absolutePath, "test").toFile().path
        srcDir = Paths.get(project.projectDir.absolutePath, "src").toFile().path
        setupCfg = Paths.get(project.projectDir.absolutePath, "setup.cfg").toFile().path

        pinnedFile = project.file("pinned.txt")

        def applicationDirectory = PythonDetailsFactory.getPythonApplicationDirectory()

        pythonEnvironment = [
            'PATH': "${ -> details.virtualEnv.toPath().resolve(applicationDirectory).toAbsolutePath().toString() }"
                    + File.pathSeparator
                    + System.getenv('PATH'),
        ]

        pythonEnvironmentDistgradle = [
            'PYGRADLE_PROJECT_NAME'   : project.name,
            'PYGRADLE_PROJECT_VERSION': "${ -> project.version }",
        ]

        defaultContainer = new PexExtension(project)
        containers = [pex: defaultContainer]

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

    public void forceVersion(String group, String name, String version) {
        Objects.requireNonNull(group, "Group cannot be null")
        Objects.requireNonNull(name, "Name cannot be null")
        Objects.requireNonNull(version, "Version cannot be null")
        forcedVersions[name] = ['group': group, 'name': name, 'version': version]
    }

    public void forceVersion(String gav) {
        Objects.requireNonNull(gav, "GAV cannot be null")

        String[] split = gav.split(':')
        if (split.length < 3) {
            throw new GradleException("Unable to parse GAV $gav")
        }

        forceVersion(split[0], split[1], split[2])
    }

    public PythonDetails getDetails() {
        return details
    }

    /**
     * Configures the {@link PythonDetails} for the project.
     *
     * @param a {@link Closure} that will delegate to {@link PythonDetails}
     */
    public void details(@DelegatesTo(PythonDetails) Closure cl) {
        cl.delegate = details
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    public Map<String, Object> getEnvironment() {
        return pythonEnvironment
    }

    File getPinnedFile() {
        return pinnedFile
    }

    void setPinnedFile(File pinnedFile) {
        this.pinnedFile = pinnedFile
    }

    /*
     * Use this as the programmatic API for getting the current container extension.
     */
    ApplicationContainer getApplicationContainer() {
        // Why am I doing it this way?  Because if the container isn't set
        // (i.e. it is null) I want to return the default container.  But if
        // it's set to a bogus value, I want it to return the null so the
        // caller will know they have a bogus value.
        return container == null ? defaultContainer : containers.get(container)
    }
}
