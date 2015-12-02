package com.linkedin.gradle.python.spec.binary;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import org.gradle.platform.base.Variant;

public interface WheelBinarySpec extends PythonBinarySpec {

    @Variant
    PythonPlatform getTargetPlatform();

    void setTargetPlatform(PythonPlatform platform);
}
