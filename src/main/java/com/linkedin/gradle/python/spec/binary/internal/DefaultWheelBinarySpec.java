package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;

public class DefaultWheelBinarySpec extends DefaultPythonBinarySpec implements WheelBinarySpec {

    private PythonPlatform pythonPlatform;

    @Override
    public PythonPlatform getTargetPlatform() {
        return pythonPlatform;
    }

    @Override
    public void setTargetPlatform(PythonPlatform platform) {
        this.pythonPlatform = platform;
    }
}
