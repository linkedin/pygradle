package com.linkedin.gradle.python.spec.binary;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;
import java.util.List;

public interface SourceDistBinarySpec extends PythonBinarySpec {

    List<PythonTargetPlatform> getTestPlatforms();

    void setTestPlatforms(List<PythonTargetPlatform> platform);
}
