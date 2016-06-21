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
package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask
import com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask
import com.linkedin.gradle.python.tasks.CheckStyleGeneratorTask
import com.linkedin.gradle.python.tasks.Flake8Task
import com.linkedin.gradle.python.tasks.InstallVirtualEnvironmentTask
import com.linkedin.gradle.python.tasks.PipInstallTask
import com.linkedin.gradle.python.tasks.PyCoverageTask
import com.linkedin.gradle.python.tasks.PyTestTask
import com.linkedin.gradle.python.tasks.SphinxDocumentationTask
import com.linkedin.gradle.python.util.FileSystemUtils
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PythonPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(PythonPlugin)

    public final static String CONFIGURATION_BOOTSTRAP_REQS = 'pygradleBootstrap'
    public final static String CONFIGURATION_SETUP_REQS = 'setupRequires'
    public final static String CONFIGURATION_BUILD_REQS = 'build'
    public final static String CONFIGURATION_DEFAULT = 'default'
    public final static String CONFIGURATION_PYDOCS = 'pydocs'
    public final static String CONFIGURATION_PYTHON = 'python'
    public final static String CONFIGURATION_TEST = 'test'
    public final static String CONFIGURATION_VENV = 'venv'
    public final static String CONFIGURATION_WHEEL = 'wheel'

    public final static String TASK_BUILD_DOCS = 'buildDocs'
    public final static String TASK_CHECK = 'check'
    public final static String TASK_COVERAGE = 'coverage'
    public final static String TASK_FLAKE = 'flake8'
    public final static String TASK_CHECKSTYLE = 'flake8Checkstyle'
    public final static String TASK_INSTALL_SETUP_REQS = 'installSetupRequirements'
    public final static String TASK_INSTALL_BUILD_REQS = 'installBuildRequirements'
    public final static String TASK_INSTALL_PROJECT = 'installProject'
    public final static String TASK_INSTALL_PYTHON_REQS = 'installPythonRequirements'
    public final static String TASK_INSTALL_TEST_REQS = 'installTestRequirements'
    public final static String TASK_PACKAGE_DOCS = 'packageDocs'
    public final static String TASK_PACKAGE_JSON_DOCS = 'packageJsonDocs'
    public final static String TASK_PYTEST = 'pytest'
    public final static String TASK_SETUP_LINKS = 'installLinks'
    public final static String TASK_VENV_CREATE = 'createVirtualEnvironment'

    public final static Map<String, Map<String, String>> PINNED_VERSIONS = [
        'argparse': ['group': 'pypi', 'name': 'argparse', 'version': '1.4.0'],
        'distgradle': ['group': 'com.linkedin.pygradle-setuptools', 'name': 'distgradle', 'version': '1.0.0'],
        'flake8': ['group': 'pypi', 'name': 'flake8', 'version': '2.5.4.1'],
        'pbr': ['group': 'pypi', 'name': 'pbr', 'version': '1.8.0'],
        'pex': ['group': 'pypi', 'name': 'pex', 'version': '1.1.4'],
        'pip': ['group': 'pypi', 'name': 'pip', 'version': '7.1.2li'],
        'pytest': ['group': 'pypi', 'name': 'pytest', 'version': '2.9.1'],
        'pytest-cov': ['group': 'pypi', 'name': 'pytest-cov', 'version': '2.2.1'],
        'pytest-xdist': ['group': 'pypi', 'name': 'pytest-xdist', 'version': '1.14'],
        'setuptools': ['group': 'pypi', 'name': 'setuptools', 'version': '19.1.1'],
        'setuptools-git': ['group': 'pypi', 'name': 'setuptools-git', 'version': '1.1'],
        'six': ['group': 'pypi', 'name': 'six', 'version': '1.10.0'],
        'Sphinx': ['group': 'pypi', 'name': 'Sphinx', 'version': '1.4.1'],
        'unittest2': ['group': 'pypi', 'name': 'unittest2', 'version': '1.1.0.1'],
        'virtualenv': ['group': 'pypi', 'name': 'virtualenv', 'version': '15.0.1'],
        'wheel': ['group': 'pypi', 'name': 'wheel', 'version': '0.26.0'],
    ]

    public final static String DOCUMENTATION_GROUP = 'documentation'

    @Override
    void apply(Project project) {

        PythonExtension settings = project.extensions.create('python', PythonExtension, project)

        project.plugins.apply('base')

        def pythonConf = project.configurations.create(CONFIGURATION_PYTHON)
        /*
         * To resolve transitive dependencies, we need the 'default' configuration
         * to extend the 'python' configuration. This is because the source
         * configuration must match the configuration which the artifact is
         * published to (i.e., 'default' in our case).
         */
        project.configurations.'default'.extendsFrom(pythonConf)

        project.configurations.create(CONFIGURATION_BOOTSTRAP_REQS)
        project.configurations.create(CONFIGURATION_SETUP_REQS)
        project.configurations.create(CONFIGURATION_BUILD_REQS)
        project.configurations.create(CONFIGURATION_PYDOCS)
        project.configurations.create(CONFIGURATION_TEST)
        project.configurations.create(CONFIGURATION_VENV)
        project.configurations.create(CONFIGURATION_WHEEL)

        /*
         * We must extend test configuration from the python configuration.
         *
         * 1. This prevents test dependencies overwriting python dependencies
         *    if they resolve to a different version transitively.
         * 2. Since tests do depend on all runtime dependencies, this is also
         *    semantically correct.
         */
        project.configurations.test.extendsFrom(pythonConf)

        /*
         * Add vended build and test dependencies to projects that apply this plugin.
         * Notice that virtualenv contains the latest versions of setuptools,
         * pip, and wheel, vended in. Make sure to use versions we can actually
         * use based on various restrictions. For example, pex may limit the
         * highest version of setuptools used. Provide the dependencies in the
         * best order they should be installed in setupRequires configuration.
         */
        project.dependencies.add(CONFIGURATION_BOOTSTRAP_REQS, PINNED_VERSIONS['virtualenv'])

        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['setuptools'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['wheel'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['pip'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['distgradle'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['setuptools-git'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS, PINNED_VERSIONS['pbr'])

        project.dependencies.add(CONFIGURATION_BUILD_REQS, PINNED_VERSIONS['flake8'])
        project.dependencies.add(CONFIGURATION_BUILD_REQS, PINNED_VERSIONS['Sphinx'])

        project.dependencies.add(CONFIGURATION_TEST, PINNED_VERSIONS['pytest'])
        project.dependencies.add(CONFIGURATION_TEST, PINNED_VERSIONS['pytest-cov'])
        project.dependencies.add(CONFIGURATION_TEST, PINNED_VERSIONS['pytest-xdist'])

        /*
         * To prevent base dependencies, such as setuptools, from installing/reinstalling, we will
         * pin their versions to values in the PINNED_VERSIONS map, which will contain known
         * good versions that satisfy all the requirements.
         *
         * TODO: Allow people to override the map that we provide for unknown use-cases
         */
        project.configurations.all {
            resolutionStrategy.eachDependency { DependencyResolveDetails details ->
                if (PINNED_VERSIONS.containsKey(details.requested.name)) {
                    String name = details.requested.name
                    String version = PINNED_VERSIONS[name].version
                    logger.lifecycle("Resolving ${name} to ${name}==${version} per gradle-python resolution strategy.")
                    details.useVersion version
                }
            }
        }

        /**
         * Install virtualenv.
         *
         * Install the virtualenv version that we implicitly depend on so that we
         * can run on systems that don't have virtualenv already installed.
         */
        project.tasks.create(TASK_VENV_CREATE, InstallVirtualEnvironmentTask.class)

        /**
         * Create a symlink to product-spec.json and config directory.
         *
         * This maintains compatibility with many of the assumptions our tooling
         * makes around Python projects and mppy.
         */
        project.tasks.create(TASK_SETUP_LINKS) {
            dependsOn project.tasks.getByName(TASK_VENV_CREATE)
            outputs.file(settings.getDetails().activateLink)

            doLast {
                def activateLinkSource = VirtualEnvExecutableHelper.getExecutable(settings, "bin/activate")
                FileSystemUtils.makeLink(project, activateLinkSource, settings.getDetails().activateLink, true)
            }
        }

        /**
         * Install core setup requirements into virtualenv.
         *
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        project.tasks.create(TASK_INSTALL_SETUP_REQS, PipInstallTask) {
            dependsOn project.tasks.getByName(TASK_SETUP_LINKS)
            args = ['--upgrade']
            installFileCollection = project.configurations.setupRequires
            sorted = false
        }

        /**
         * Install build requirements into virtualenv.
         *
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        project.tasks.create(TASK_INSTALL_BUILD_REQS, PipInstallTask) {
            dependsOn project.tasks.getByName(TASK_INSTALL_SETUP_REQS)
            args = ['--upgrade']
            installFileCollection = project.configurations.build
        }

        /**
         * Install the product's Python requirements.
         *
         * A products Python requirements are those listed in the product-spec.json in the external or product sections.
         *
         */
        project.tasks.create(TASK_INSTALL_PYTHON_REQS, PipInstallTask) {
            dependsOn project.tasks.getByName(TASK_INSTALL_BUILD_REQS)
            installFileCollection = project.configurations.python
        }

        /**
         * Install the product's Python test requirements.
         *
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        project.tasks.create(TASK_INSTALL_TEST_REQS, PipInstallTask) {
            dependsOn project.tasks.getByName(TASK_INSTALL_PYTHON_REQS)
            installFileCollection = project.configurations.test
        }

        /**
         * Install the product itself.
         *
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        project.tasks.create(TASK_INSTALL_PROJECT, PipInstallTask) {
            dependsOn project.tasks.getByName(TASK_INSTALL_TEST_REQS)
            installFileCollection = project.files(project.file(project.projectDir))
            args = ['--editable']
            environment = settings.pythonEnvironmentDistgradle
        }

        /**
         * Any task that extends from {@link AbstractPythonMainSourceDefaultTask} will require TASK_INSTALL_BUILD_REQS
         */
        project.tasks.withType(AbstractPythonMainSourceDefaultTask.class) { Task task ->
            task.dependsOn project.tasks.getByName(TASK_INSTALL_BUILD_REQS)
        }

        /**
         * Any task that extends from {@link AbstractPythonTestSourceDefaultTask} will require TASK_INSTALL_PROJECT
         */
        project.tasks.withType(AbstractPythonTestSourceDefaultTask.class) { Task task ->
            task.dependsOn project.tasks.getByName(TASK_INSTALL_PROJECT)
        }

        /**
         * Run tests using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        project.tasks.create(TASK_PYTEST, PyTestTask.class)

        // Add a dependency to task ``check`` to depend on our Python plugin's ``pytest`` task
        project.tasks.getByName(TASK_CHECK).dependsOn(project.tasks.getByName(TASK_PYTEST))

        /**
         * Run coverage using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        project.tasks.create(TASK_COVERAGE, PyCoverageTask.class)

        /**
         * Run flake8.
         *
         * This uses the ``setup.cfg`` if present to configure flake8.
         */
        project.tasks.create(TASK_FLAKE, Flake8Task)

        /**
         * Create checkstyle styled report from flake
         */
        project.tasks.create(TASK_CHECKSTYLE, CheckStyleGeneratorTask)

        // Add a dependency to task ``check`` to depend on our Python plugin's ``flake8`` task
        project.tasks.getByName(TASK_CHECK).dependsOn(project.tasks.getByName(TASK_FLAKE))

        /**
         * Create documentation using Sphinx.
         */
        def sphinxHtml = project.tasks.create(TASK_BUILD_DOCS + 'Html', SphinxDocumentationTask) {
            type = SphinxDocumentationTask.DocType.HTML
            dependsOn TASK_INSTALL_PROJECT
        }

        def sphinxJson = project.tasks.create(TASK_BUILD_DOCS + 'Json', SphinxDocumentationTask) {
            type = SphinxDocumentationTask.DocType.JSON
            dependsOn TASK_INSTALL_PROJECT
        }

        project.tasks.withType(SphinxDocumentationTask.class).each { SphinxDocumentationTask task ->
            task.group = DOCUMENTATION_GROUP
            task.description = "Generate Sphinx documentation in ${task.type.builderName} format"
        }

        project.tasks.create(TASK_BUILD_DOCS) {
            dependsOn sphinxHtml, sphinxJson
        }

        /**
         * Tar up the documentation.
         */
        def packageDocsTask = project.tasks.create(TASK_PACKAGE_DOCS, Tar.class, new PackageDocumentationAction(sphinxHtml));
        packageDocsTask.dependsOn(project.tasks.getByName(TASK_BUILD_DOCS))

        /**
         * Tar up the JSON documentation.
         */
        def packageJsonDocsTask = project.tasks.create(TASK_PACKAGE_JSON_DOCS, Tar.class, new PackageDocumentationAction(sphinxJson))
        packageJsonDocsTask.dependsOn(project.tasks.getByName(TASK_BUILD_DOCS))

        // Only build the artifact if the docs source directory actually exists
        if (project.file(settings.docsDir).exists()) {
            project.artifacts.add(CONFIGURATION_PYDOCS, packageDocsTask)
            project.artifacts.add(CONFIGURATION_PYDOCS, packageJsonDocsTask)
        }

    }

    static class PackageDocumentationAction implements Action<Tar> {

        private final SphinxDocumentationTask documentationTask

        PackageDocumentationAction(SphinxDocumentationTask documentationTask) {
            this.documentationTask = documentationTask
        }

        @Override
        void execute(Tar tar) {
            tar.compression = Compression.GZIP
            tar.baseName = tar.project.name
            tar.classifier = 'docs-' + documentationTask.type.builderName
            tar.extension = 'tar.gz'
            tar.from(documentationTask)
            tar.into("${tar.getBaseName()}-${tar.getVersion()}-${tar.getClassifier()}")
        }
    }
}
