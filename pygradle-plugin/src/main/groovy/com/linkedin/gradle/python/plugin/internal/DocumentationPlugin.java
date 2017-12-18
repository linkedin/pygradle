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
package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.SphinxDocumentationTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYDOCS;
import static com.linkedin.gradle.python.util.StandardTextValues.DOCUMENTATION_GROUP;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_BUILD_DOCS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_PROJECT;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_PACKAGE_DOCS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_PACKAGE_JSON_DOCS;

public class DocumentationPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {

        PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        /*
         * Create documentation using Sphinx.
         */
        final SphinxDocumentationTask sphinxHtml = project.getTasks().create(TASK_BUILD_DOCS.getValue() + "Html", SphinxDocumentationTask.class, task -> {
            task.type = SphinxDocumentationTask.DocType.HTML;
            task.dependsOn(TASK_INSTALL_PROJECT.getValue());
            task.onlyIf(it -> project.file(settings.docsDir).exists());
        });

        final SphinxDocumentationTask sphinxJson = project.getTasks().create(TASK_BUILD_DOCS.getValue() + "Json", SphinxDocumentationTask.class, task -> {
            task.type = SphinxDocumentationTask.DocType.JSON;
            task.dependsOn(TASK_INSTALL_PROJECT.getValue());
            task.onlyIf(it -> project.file(settings.docsDir).exists());
        });

        project.getTasks().withType(SphinxDocumentationTask.class, task -> {
            task.setGroup(DOCUMENTATION_GROUP.getValue());
            task.setDescription("Generate Sphinx documentation in " + task.type.getBuilderName() + " format");
        });

        project.getTasks().create(TASK_BUILD_DOCS.getValue(), it -> it.dependsOn(sphinxHtml, sphinxJson));

        /*
         * Tar up the documentation.
         */
        Tar packageDocsTask = project.getTasks().create(TASK_PACKAGE_DOCS.getValue(), Tar.class, new PackageDocumentationAction(sphinxHtml));
        packageDocsTask.dependsOn(project.getTasks().getByName(TASK_BUILD_DOCS.getValue()));

        /*
         * Tar up the JSON documentation.
         */
        Tar packageJsonDocsTask = project.getTasks().create(TASK_PACKAGE_JSON_DOCS.getValue(), Tar.class, new PackageDocumentationAction(sphinxJson));
        packageJsonDocsTask.dependsOn(project.getTasks().getByName(TASK_BUILD_DOCS.getValue()));

        // Only build the artifact if the docs source directory actually exists
        if (project.file(settings.docsDir).exists()) {
            project.getArtifacts().add(CONFIGURATION_PYDOCS.getValue(), packageDocsTask);
            project.getArtifacts().add(CONFIGURATION_PYDOCS.getValue(), packageJsonDocsTask);
        }

    }

    private static class PackageDocumentationAction implements Action<Tar> {
        private final SphinxDocumentationTask documentationTask;

        PackageDocumentationAction(SphinxDocumentationTask documentationTask) {
            this.documentationTask = documentationTask;
        }

        @Override
        public void execute(final Tar tar) {
            tar.setCompression(Compression.GZIP);
            tar.setBaseName(tar.getProject().getName());
            tar.setClassifier("docs-" + documentationTask.type.getBuilderName());
            tar.setExtension("tar.gz");
            tar.from(documentationTask);
            tar.into(tar.getBaseName() + "-" + tar.getVersion() + "-" + tar.getClassifier());
        }
    }
}
