package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.plugin.internal.PythonBinarySpecAction;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.util.List;

public class PythonSourceDistSpecAction extends PythonBinarySpecAction<SourceDistBinarySpec> {

    private final List<PythonTargetPlatform> platformList;

    public PythonSourceDistSpecAction(List<PythonTargetPlatform> platformList) {
        this.platformList = platformList;
    }

    @Override
    public void execute(SourceDistBinarySpec spec) {
        spec.setTestPlatforms(platformList);
    }
}
