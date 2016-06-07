package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.ExecResult


/**
 * Generates Sphinx Documentation
 */
@CompileStatic
class SphinxDocumentationTask extends AbstractPythonMainSourceDefaultTask {

    @OutputDirectory
    File getDocDir() {
        def typeString = type.toString().toLowerCase()
        return new File(project.buildDir, "docs/${typeString}/${project.name}-${ -> project.version }-docs-${typeString}")
    }

    /**
     * The type of documentation that will be generated
     */
    @Input
    public DocType type

    @Override
    public void preExecution() {
        args(VirtualEnvExecutableHelper.getExecutable(component, "bin/sphinx-build").absolutePath,
            '-b', type.builderName,
            project.file(component.docsDir).getAbsolutePath(),
            "${getDocDir().getAbsolutePath()}")
    }

    @Override
    void processResults(ExecResult execResult) {
    }

    /**
     * Types of documentation that are supported by Sphinx
     */
    public enum DocType {
        JSON('json'),
        HTML('html')

        final String builderName;

        DocType(String name) {
            builderName = name
        }
    }
}
