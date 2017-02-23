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
import com.linkedin.gradle.python.tasks.SphinxDocumentationTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import static com.linkedin.gradle.python.util.StandardTextValuesConfiguration.PYDOCS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.BUILD_DOCS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.BUILD_DOCS_HTML
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.BUILD_DOCS_JSON
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.INSTALL_PROJECT
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.PACKAGE_DOCS
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.PACKAGE_JSON_DOCS

/**
 * This plugin encapsulates the documentation system.  This contains everything that is needed for that.
 */
class PythonSphinxBasePlugin extends AbstractPluginBase {

    @Override
    void apply(Project project) {
        this.project = project

        createConfiguration(PYDOCS)

        PythonExtension settings = addGetExtensionLocal(PYTHON_EXTENSION_NAME, PythonExtension)
        createDependenciesSphinx(settings)

        def sphinxHtml = addTaskLocal([name: BUILD_DOCS_HTML, type: SphinxDocumentationTask]) {
            type = SphinxDocumentationTask.DocType.HTML
        }
        sphinxHtml.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        def sphinxJson = addTaskLocal([name: BUILD_DOCS_JSON, type: SphinxDocumentationTask]) {
            type = SphinxDocumentationTask.DocType.JSON
        }
        sphinxJson.onlyIf {
            project.file(project.python.docsDir).exists()
        }

        addTaskLocal([name: BUILD_DOCS])

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

        aDependsOnB(BUILD_DOCS_HTML, INSTALL_PROJECT)
        aDependsOnB(BUILD_DOCS_JSON, INSTALL_PROJECT)

        aDependsOnB(BUILD_DOCS, BUILD_DOCS_JSON)
        aDependsOnB(BUILD_DOCS, BUILD_DOCS_HTML)

        aDependsOnB(PACKAGE_JSON_DOCS, BUILD_DOCS)
        aDependsOnB(PACKAGE_DOCS, BUILD_DOCS)
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
