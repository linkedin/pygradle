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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import static com.linkedin.gradle.python.util.StandardTextValuesConfiguration.*
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.*

@SuppressWarnings("AbcMetric")
//TODO: Break apart method
class PythonPlugin extends AbstractPluginBase {

    @Override
    @SuppressWarnings(["MethodSize", "AbcMetric"])
    //TODO: Break apart method
    void apply(Project project) {
        this.project = project

        PythonExtension settings = project.extensions.create('python', PythonExtension, project)

        project.plugins.apply('base')

        createConfigurations()
        createCoreDependencies(settings)

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

        addTaskLocal([name: PIN_REQUIREMENTS, type: PinRequirementsTask])
        addTaskLocal([name: VENV_CREATE,
                      type: InstallVirtualEnvironmentTask]){ task ->
            task.pythonDetails = settings.details
        }

        addTaskLocal([name: SETUP_LINKS]) {
            outputs.file(settings.getDetails().activateLink)

            doLast {
                def activateLinkSource = settings.getDetails().virtualEnvironment.getScript("activate")
                def activateLink = settings.getDetails().activateLink
                FileSystemUtils.makeSymLink(activateLinkSource, activateLink)
            }
        }
        addTaskLocal([name : CLEAN_SAVE_VENV, type : CleanSaveVenvTask])

        /**
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        addTaskLocal([name : INSTALL_SETUP_REQS,
                      type : PipInstallTask]) {
            pythonDetails = settings.details
            args = ['--upgrade']
            installFileCollection = project.configurations.setupRequires
            sorted = false
        }

        /**
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        addTaskLocal([name : INSTALL_BUILD_REQS,
                      type : PipInstallTask]) {
            pythonDetails = settings.details
            args = ['--upgrade']
            installFileCollection = project.configurations.build
        }

        /**
         * A products Python requirements are those listed in the product-spec.json in the external or product sections.
         */
        addTaskLocal([name : INSTALL_PYTHON_REQS,
                      type : PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.configurations.python
        }

        /**
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        addTaskLocal([name : INSTALL_TEST_REQS,
                      type : PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.configurations.test
        }

        /**
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        addTaskLocal([name : INSTALL_PROJECT,
                      type : PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.files(project.file(project.projectDir))
            args = ['--editable']
            environment = settings.pythonEnvironmentDistgradle
        }

        /**
         * Any task that extends from {@link AbstractPythonMainSourceDefaultTask} will require INSTALL_BUILD_REQS
         */
        project.tasks.withType(AbstractPythonMainSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(INSTALL_BUILD_REQS.value)
        }

        /**
         * Any task that extends from {@link AbstractPythonTestSourceDefaultTask} will require INSTALL_PROJECT
         */
        project.tasks.withType(AbstractPythonTestSourceDefaultTask) { Task task ->
            task.dependsOn project.tasks.getByName(INSTALL_PROJECT.value)
        }

        def pyTestTask = addTaskLocal([name: PYTEST, type: PyTestTask])

        pyTestTask.onlyIf {
            project.file(project.python.testDir).exists()
        }

        def pyCheckTask = addTaskLocal([name: COVERAGE, type: PyCoverageTask])
        pyCheckTask.onlyIf {
            project.file(project.python.testDir).exists()
        }

        def flakeTask = addTaskLocal([name: FLAKE, type: Flake8Task])

        flakeTask.onlyIf {
            Integer paths = 0
            if (project.file(project.python.srcDir).exists()) {
                ++paths
            }
            if (project.file(project.python.testDir).exists()) {
                ++paths
            }

            paths > 0
        }

        addTaskLocal([name: CHECKSTYLE, type: CheckStyleGeneratorTask])

        def sphinxHtml = addTaskLocal([name : BUILD_DOCS_HTML,
                                       type : SphinxDocumentationTask]) {
            type = SphinxDocumentationTask.DocType.HTML
        }
        sphinxHtml.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        def sphinxJson = addTaskLocal([name : BUILD_DOCS_JSON,
                                       type : SphinxDocumentationTask]) {
            type = SphinxDocumentationTask.DocType.JSON
        }
        sphinxJson.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        project.tasks.withType(SphinxDocumentationTask).each { SphinxDocumentationTask task ->
            task.description = "Generate Sphinx documentation in ${task.type.builderName} format"
        }

        addTaskLocal([name : BUILD_DOCS])

        def packageDocsTask = addTaskLocal([name  : PACKAGE_DOCS,
                                            type  : Tar,
                                            action: new PackageDocumentationAction(sphinxHtml as SphinxDocumentationTask)])

        def packageJsonDocsTask = addTaskLocal([name  : PACKAGE_JSON_DOCS,
                                                type  : Tar,
                                                action: new PackageDocumentationAction(sphinxJson as SphinxDocumentationTask)])


        // Only build the artifact if the docs source directory actually exists
        if (project.file(settings.docsDir).exists()) {
            project.artifacts.add(PYDOCS.value, packageDocsTask)
            project.artifacts.add(PYDOCS.value, packageJsonDocsTask)
        }

        addTaskLocal([name: SETUP_PY_WRITER, type: GenerateSetupPyTask])


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
        aDependsOnB(VENV_CREATE, PIN_REQUIREMENTS)
        aDependsOnB(SETUP_LINKS, VENV_CREATE)
        aDependsOnB(INSTALL_SETUP_REQS, SETUP_LINKS)
        aDependsOnB(INSTALL_BUILD_REQS, INSTALL_SETUP_REQS)
        aDependsOnB(INSTALL_PYTHON_REQS, INSTALL_BUILD_REQS)
        aDependsOnB(INSTALL_TEST_REQS, INSTALL_PYTHON_REQS)
        aDependsOnB(INSTALL_PROJECT, INSTALL_TEST_REQS)
        aDependsOnB(CHECK, PYTEST)
        aDependsOnB(CHECK, FLAKE)
        aDependsOnB(BUILD_DOCS_HTML, INSTALL_PROJECT)
        aDependsOnB(BUILD_DOCS_JSON, INSTALL_PROJECT)
        aDependsOnB(BUILD_DOCS, BUILD_DOCS_HTML)
        aDependsOnB(BUILD_DOCS, BUILD_DOCS_JSON)
        aDependsOnB(PACKAGE_DOCS, BUILD_DOCS)
        aDependsOnB(PACKAGE_JSON_DOCS, BUILD_DOCS)
    }

    def createConfigurations() {
        def pythonConf = createConfiguration(PYTHON)
        /*
         * To resolve transitive dependencies, we need the 'default' configuration
         * to extend the 'python' configuration. This is because the source
         * configuration must match the configuration which the artifact is
         * published to (i.e., 'default' in our case).
         */
        project.configurations.'default'.extendsFrom(pythonConf)

        createConfiguration(BOOTSTRAP_REQS)
        createConfiguration(SETUP_REQS)
        createConfiguration(BUILD_REQS)
        createConfiguration(PYDOCS)
        createConfiguration(TEST)
        createConfiguration(VENV)
        createConfiguration(WHEEL)

        /*
         * We must extend test configuration from the python configuration.
         *
         * 1. This prevents test dependencies overwriting python dependencies
         *    if they resolve to a different version transitively.
         * 2. Since tests do depend on all runtime dependencies, this is also
         *    semantically correct.
         */
        project.configurations.test.extendsFrom(pythonConf)
    }
    /**
     * Add vended build and test dependencies to projects that apply this plugin.
     * Notice that virtualenv contains the latest versions of setuptools,
     * pip, and wheel, vended in. Make sure to use versions we can actually
     * use based on various restrictions. For example, pex may limit the
     * highest version of setuptools used. Provide the dependencies in the
     * best order they should be installed in setupRequires configuration.
     */
    def createCoreDependencies(PythonExtension settings) {
        addDependency(BOOTSTRAP_REQS, settings.forcedVersions['virtualenv'])

        addDependency(SETUP_REQS, settings.forcedVersions['appdirs'])
        addDependency(SETUP_REQS, settings.forcedVersions['packaging'])
        addDependency(SETUP_REQS, settings.forcedVersions['wheel'])
        addDependency(SETUP_REQS, settings.forcedVersions['setuptools'])
        addDependency(SETUP_REQS, settings.forcedVersions['pip'])
        addDependency(SETUP_REQS, settings.forcedVersions['setuptools-git'])
        addDependency(SETUP_REQS, settings.forcedVersions['pbr'])

        addDependency(BUILD_REQS, settings.forcedVersions['flake8'])
        addDependency(BUILD_REQS, settings.forcedVersions['Sphinx'])

        addDependency(TEST, settings.forcedVersions['pytest'])
        addDependency(TEST, settings.forcedVersions['pytest-cov'])
        addDependency(TEST, settings.forcedVersions['pytest-xdist'])
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
