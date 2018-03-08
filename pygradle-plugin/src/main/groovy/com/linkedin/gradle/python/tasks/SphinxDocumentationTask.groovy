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
package com.linkedin.gradle.python.tasks

import groovy.transform.CompileStatic
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.ExecResult

/**
 * Generates Sphinx Documentation
 */
@CompileStatic
class SphinxDocumentationTask extends AbstractPythonMainSourceDefaultTask {

    @InputFiles
    FileCollection getDocFiles() {
        return getProject().fileTree(getComponent().docsDir)
    }

    @OutputDirectory
    File getDocDir() {
        def typeString = type.toString().toLowerCase()
        return new File(project.buildDir, "docs/${ typeString }/${ project.name }-${ -> project.version }-docs-${ typeString }")
    }

    /**
     * The type of documentation that will be generated
     */
    @Input
    public DocType type

    @Override
    void preExecution() {
        args(pythonDetails.virtualEnvironment.findExecutable('sphinx-build').absolutePath,
            '-b', type.builderName,
            project.file(component.docsDir).getAbsolutePath(),
            "${ getDocDir().getAbsolutePath() }")
    }

    @Override
    void processResults(ExecResult execResult) {
    }

    /**
     * Types of documentation that are supported by Sphinx
     */
    enum DocType {
        JSON('json'),
        HTML('html')

        final String builderName

        DocType(String name) {
            builderName = name
        }
    }
}
