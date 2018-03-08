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
package com.linkedin.gradle.python.util.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout

public class PyPiRepoUtil {

    private PyPiRepoUtil() {
        // noop
    }

    public static void setupPyGradleRepo(Project project) {
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
}
