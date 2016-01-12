package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.plugin.internal.PythonBinarySpecAction;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;

import java.io.File;
import java.util.List;

public class PythonSourceDistSpecAction extends PythonBinarySpecAction<SourceDistBinarySpec> {

    private final File buildDir;

    public PythonSourceDistSpecAction(File buildDir) {
        this.buildDir = buildDir;
    }

    @Override
    public void execute(SourceDistBinarySpec spec) {
        spec.setBuildDir(buildDir);
    }
}
