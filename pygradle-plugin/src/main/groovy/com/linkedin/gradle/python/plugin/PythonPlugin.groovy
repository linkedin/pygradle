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

import com.linkedin.gradle.python.tasks.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout

import static com.linkedin.gradle.python.util.values.PyGradleTask.*

@SuppressWarnings("AbcMetric")
class PythonPlugin extends AbstractPluginBase {

    @Override
    void applyTo(Project project) {



        addPluginLocal(PythonVenvPlugin)
        addPluginLocal(PythonSphinxBasePlugin)

        createDependenciesPython(settings)

        /**
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        addTaskLocal([name: INSTALL_PROJECT, type: PipInstallTask]) {
            pythonDetails = settings.details
            installFileCollection = project.files(project.file(project.projectDir))
            args = ['--editable']
            environment = settings.pythonEnvironmentDistgradle
        }

        def pyTestTask = addTaskLocal([name: PYTEST, type: PyTestTask])

        pyTestTask.onlyIf {
            project.file(settings.testDir).exists()
        }

        def pyCheckTask = addTaskLocal([name: COVERAGE, type: PyCoverageTask])
        pyCheckTask.onlyIf {
            project.file(settings.testDir).exists()
        }

        def flakeTask = addTaskLocal([name: FLAKE, type: Flake8Task])

        flakeTask.onlyIf {
            Integer paths = 0
            if (project.file(settings.srcDir).exists()) {
                ++paths
            }
            if (project.file(settings.testDir).exists()) {
                ++paths
            }

            paths > 0
        }

        addTaskLocal([name: CHECKSTYLE, type: CheckStyleGeneratorTask])
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

        aDependsOnB(CHECK, PYTEST)
        aDependsOnB(CHECK, FLAKE)
        aDependsOnB(INSTALL_PROJECT, INSTALL_TEST_REQS)

    }
}
