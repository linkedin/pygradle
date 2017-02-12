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
import com.linkedin.gradle.python.tasks.*
import com.linkedin.gradle.python.util.FileSystemUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import static com.linkedin.gradle.python.util.StandardTextValues.*

@SuppressWarnings("AbcMetric")
//TODO: Break apart method
class PythonPlugin implements Plugin<Project> {

    @Override
    @SuppressWarnings(["MethodSize", "AbcMetric"])
    //TODO: Break apart method
    void apply(Project project) {

        PythonExtension settings = project.extensions.create('python', PythonExtension, project)

        project.plugins.apply('base')

        def pythonConf = project.configurations.create(CONFIGURATION_PYTHON.value)
        /*
         * To resolve transitive dependencies, we need the 'default' configuration
         * to extend the 'python' configuration. This is because the source
         * configuration must match the configuration which the artifact is
         * published to (i.e., 'default' in our case).
         */
        project.configurations.'default'.extendsFrom(pythonConf)

        project.configurations.create(CONFIGURATION_BOOTSTRAP_REQS.value)
        project.configurations.create(CONFIGURATION_SETUP_REQS.value)
        project.configurations.create(CONFIGURATION_BUILD_REQS.value)
        project.configurations.create(CONFIGURATION_PYDOCS.value)
        project.configurations.create(CONFIGURATION_TEST.value)
        project.configurations.create(CONFIGURATION_VENV.value)
        project.configurations.create(CONFIGURATION_WHEEL.value)

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
        project.dependencies.add(CONFIGURATION_BOOTSTRAP_REQS.value, settings.forcedVersions['virtualenv'])

        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['appdirs'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['packaging'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['wheel'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['setuptools'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['pip'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['setuptools-git'])
        project.dependencies.add(CONFIGURATION_SETUP_REQS.value, settings.forcedVersions['pbr'])

        project.dependencies.add(CONFIGURATION_BUILD_REQS.value, settings.forcedVersions['flake8'])
        project.dependencies.add(CONFIGURATION_BUILD_REQS.value, settings.forcedVersions['Sphinx'])

        project.dependencies.add(CONFIGURATION_TEST.value, settings.forcedVersions['pytest'])
        project.dependencies.add(CONFIGURATION_TEST.value, settings.forcedVersions['pytest-cov'])
        project.dependencies.add(CONFIGURATION_TEST.value, settings.forcedVersions['pytest-xdist'])

        /*
         * To prevent base dependencies, such as setuptools, from installing/reinstalling, we will
         * pin their versions to values in the extension.forcedVersions map, which will contain known
         * good versions that satisfy all the requirements.
         */
        def dependencyResolveDetails = new PyGradleDependencyResolveDetails(settings.forcedVersions)
        //noinspection GroovyAssignabilityCheck
        project.configurations.each { configuration ->
            configuration.resolutionStrategy.eachDependency(dependencyResolveDetails)
        }

        /*
         * Write the direct dependencies into a requirements file as a list of pinned versions.
         */
        def pinRequirementsTask = project.tasks.create(TASK_PIN_REQUIREMENTS.value, PinRequirementsTask)

        /**
         * Install virtualenv.
         *
         * Install the virtualenv version that we implicitly depend on so that we
         * can run on systems that don't have virtualenv already installed.
         */
        project.tasks.create(TASK_VENV_CREATE.value, InstallVirtualEnvironmentTask) { task ->
            task.dependsOn pinRequirementsTask
            task.pythonDetails = settings.details
        }

        /**
         * Creates a link so users can activate into the virtual environment.
         */
        project.tasks.create(TASK_SETUP_LINKS.value) {
            dependsOn project.tasks.getByName(TASK_VENV_CREATE.value)
            outputs.file(settings.getDetails().activateLink)

            doLast {
                def activateLinkSource = settings.getDetails().virtualEnvironment.getScript("activate")
                def activateLink = settings.getDetails().activateLink
                FileSystemUtils.makeSymLink(activateLinkSource, activateLink)
            }
        }

        /**
         * task that cleans the project but leaves the venv in tact.  Helpful for projects on windows that
         * take a very long time to build the venv.
         */
        project.tasks.create([name: TASK_CLEAN_SAVE_VENV, type: CleanSaveVenvTask, group: BUILD_GROUP])

        /**
         * Install core setup requirements into virtualenv.
         *
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        project.tasks.create(TASK_INSTALL_SETUP_REQS.value, PipInstallTask) {
            pythonDetails = settings.details
            dependsOn project.tasks.getByName(TASK_SETUP_LINKS.value)
            args = ['--upgrade']
            installFileCollection = project.configurations.setupRequires
            sorted = false
        }

        /**
         * Install build requirements into virtualenv.
         *
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        project.tasks.create(TASK_INSTALL_BUILD_REQS.value, PipInstallTask) {
            pythonDetails = settings.details
            dependsOn project.tasks.getByName(TASK_INSTALL_SETUP_REQS.value)
            args = ['--upgrade']
            installFileCollection = project.configurations.build
        }

        /**
         * Install the product's Python requirements.
         *
         * A products Python requirements are those listed in the product-spec.json in the external or product sections.
         *
         */
        project.tasks.create(TASK_INSTALL_PYTHON_REQS.value, PipInstallTask) {
            pythonDetails = settings.details
            dependsOn project.tasks.getByName(TASK_INSTALL_BUILD_REQS.value)
            installFileCollection = project.configurations.python
        }

        /**
         * Install the product's Python test requirements.
         *
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        project.tasks.create(TASK_INSTALL_TEST_REQS.value, PipInstallTask) {
            pythonDetails = settings.details
            dependsOn project.tasks.getByName(TASK_INSTALL_PYTHON_REQS.value)
            installFileCollection = project.configurations.test
        }

        /**
         * Install the product itself.
         *
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        project.tasks.create(TASK_INSTALL_PROJECT.value, PipInstallTask) {
            pythonDetails = settings.details
            dependsOn project.tasks.getByName(TASK_INSTALL_TEST_REQS.value)
            installFileCollection = project.files(project.file(project.projectDir))
            args = ['--editable']
            environment = settings.pythonEnvironmentDistgradle
        }

        /**
         * Any task that extends from {@link AbstractPythonMainSourceDefaultTask} will require TASK_INSTALL_BUILD_REQS
         */
        project.tasks.withType(AbstractPythonMainSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(TASK_INSTALL_BUILD_REQS.value)
        }

        /**
         * Any task that extends from {@link AbstractPythonTestSourceDefaultTask} will require TASK_INSTALL_PROJECT
         */
        project.tasks.withType(AbstractPythonTestSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(TASK_INSTALL_PROJECT.value)
        }

        /**
         * Run tests using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        def pyTestTask = project.tasks.create(TASK_PYTEST.value, PyTestTask)

        pyTestTask.onlyIf {
            project.file(project.python.testDir).exists()
        }

        // Add a dependency to task ``check`` to depend on our Python plugin's ``pytest`` task
        project.tasks.getByName(TASK_CHECK.value).dependsOn(project.tasks.getByName(TASK_PYTEST.value))

        /**
         * Run coverage using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        def pyCheckTask = project.tasks.create(TASK_COVERAGE.value, PyCoverageTask)
        pyCheckTask.onlyIf {
            project.file(project.python.testDir).exists()
        }

        /**
         * Run flake8.
         *
         * This uses the ``setup.cfg`` if present to configure flake8.
         */
        def flakeTask = project.tasks.create(TASK_FLAKE.value, Flake8Task)

        flakeTask.onlyIf {
            Integer paths = 0
            if (project.file(project.python.srcDir).exists()) { ++paths }
            if (project.file(project.python.testDir).exists()) { ++paths }

            paths > 0
        }

        /**
         * Create checkstyle styled report from flake
         */
        project.tasks.create(TASK_CHECKSTYLE.value, CheckStyleGeneratorTask)

        // Add a dependency to task ``check`` to depend on our Python plugin's ``flake8`` task
        project.tasks.getByName(TASK_CHECK.value).dependsOn(project.tasks.getByName(TASK_FLAKE.value))

        /**
         * Create documentation using Sphinx.
         */
        def sphinxHtml = project.tasks.create(TASK_BUILD_DOCS.value + 'Html', SphinxDocumentationTask) {
            type = SphinxDocumentationTask.DocType.HTML
            dependsOn TASK_INSTALL_PROJECT.value
        }
        sphinxHtml.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        def sphinxJson = project.tasks.create(TASK_BUILD_DOCS.value + 'Json', SphinxDocumentationTask) {
            type = SphinxDocumentationTask.DocType.JSON
            dependsOn TASK_INSTALL_PROJECT.value
        }
        sphinxJson.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        project.tasks.withType(SphinxDocumentationTask).each { SphinxDocumentationTask task ->
            task.group = DOCUMENTATION_GROUP.value
            task.description = "Generate Sphinx documentation in ${task.type.builderName} format"
        }

        project.tasks.create(TASK_BUILD_DOCS.value) {
            dependsOn sphinxHtml, sphinxJson
        }

        /**
         * Tar up the documentation.
         */
        def packageDocsTask = project.tasks.create(TASK_PACKAGE_DOCS.value, Tar, new PackageDocumentationAction(sphinxHtml))
        packageDocsTask.dependsOn(project.tasks.getByName(TASK_BUILD_DOCS.value))

        /**
         * Tar up the JSON documentation.
         */
        def packageJsonDocsTask = project.tasks.create(TASK_PACKAGE_JSON_DOCS.value, Tar, new PackageDocumentationAction(sphinxJson))
        packageJsonDocsTask.dependsOn(project.tasks.getByName(TASK_BUILD_DOCS.value))

        // Only build the artifact if the docs source directory actually exists
        if (project.file(settings.docsDir).exists()) {
            project.artifacts.add(CONFIGURATION_PYDOCS.value, packageDocsTask)
            project.artifacts.add(CONFIGURATION_PYDOCS.value, packageJsonDocsTask)
        }

        project.tasks.create(TASK_SETUP_PY_WRITER.value, GenerateSetupPyTask)

        project.getRepositories().metaClass.pyGradlePyPi = { ->
            delegate.ivy(new Action<IvyArtifactRepository>() {
                @Override
                void execute(IvyArtifactRepository ivyArtifactRepository) {
                    ivyArtifactRepository.setName('pygradle-pypi')
                    ivyArtifactRepository.setUrl('https://linkedin.jfrog.io/linkedin/pypi-external/')
                    ivyArtifactRepository.layout("pattern", new Action<IvyPatternRepositoryLayout>() {
                        @Override
                        void execute(IvyPatternRepositoryLayout repositoryLayout) {
                            repositoryLayout.artifact('[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]')
                            repositoryLayout.ivy('[organisation]/[module]/[revision]/[module]-[revision].ivy')
                            repositoryLayout.setM2compatible(true)
                        }
                    })
                }
            })
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
