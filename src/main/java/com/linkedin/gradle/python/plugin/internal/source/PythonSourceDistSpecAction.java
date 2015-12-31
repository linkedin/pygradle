package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.plugin.internal.PythonBinarySpecAction;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;

import java.io.File;
import java.util.List;

public class PythonSourceDistSpecAction extends PythonBinarySpecAction<SourceDistBinarySpec> {

    private final List<PythonTargetPlatform> platformList;
    private final File buildDir;

    public PythonSourceDistSpecAction(List<PythonTargetPlatform> platformList, File buildDir) {
        this.platformList = platformList;
        this.buildDir = buildDir;
    }

    @Override
    public void execute(SourceDistBinarySpec spec) {
        spec.setBuildDir(buildDir);
        spec.setTestPlatforms(platformList);
    }
}
