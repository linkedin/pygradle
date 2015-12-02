package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSourceDistBinarySpec extends DefaultPythonBinarySpec implements SourceDistBinarySpec {

    private final List<PythonPlatform> platformList = new ArrayList<PythonPlatform>();

    @Override
    public List<PythonPlatform> getTargetPlatforms() {
        return Collections.unmodifiableList(platformList);
    }

    @Override
    public void setTargetPlatforms(List<PythonPlatform> platform) {
        platformList.clear();
        platformList.addAll(platform);
    }
}
