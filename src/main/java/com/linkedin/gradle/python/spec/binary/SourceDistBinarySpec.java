package com.linkedin.gradle.python.spec.binary;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;

import java.util.List;

public interface SourceDistBinarySpec extends PythonBinarySpec {

    List<PythonPlatform> getTargetPlatforms();

    void setTargetPlatforms(List<PythonPlatform> platform);
}
