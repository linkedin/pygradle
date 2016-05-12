package com.linkedin.gradle.python.tasks;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;


/**
 * Add's lazy evaluation of the tests directory, see {@link AbstractPythonMainSourceDefaultTask} for more details.
 */
abstract public class AbstractPythonTestSourceDefaultTask extends AbstractPythonMainSourceDefaultTask {

    FileTree testSource;

    AbstractPythonTestSourceDefaultTask() {
    }

    @InputFiles
    FileCollection getTestFiles() {
        ConfigurableFileTree componentFiles = getProject().fileTree(getComponent().testDir);
        componentFiles.exclude(standardExcludes());
        if (testSource != null) {
            return testSource.plus(componentFiles);
        }
        return componentFiles;
    }
}
