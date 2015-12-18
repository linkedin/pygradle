package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSourceDistBinarySpec extends DefaultPythonBinarySpec implements SourceDistBinarySpec {

    private final List<PythonTargetPlatform> platformList = new ArrayList<PythonTargetPlatform>();

    public DefaultSourceDistBinarySpec() {
        super("Source");
    }

    @Override
    public List<PythonTargetPlatform> getTestPlatforms() {
        return Collections.unmodifiableList(platformList);
    }

    @Override
    public void setTestPlatforms(List<PythonTargetPlatform> platform) {
        platformList.clear();
        platformList.addAll(platform);
    }
}
