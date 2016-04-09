package com.linkedin.gradle.python.spec.binary.internal;

public class DefaultWheelBinarySpec extends DefaultPythonBinarySpec implements WheelBinarySpecInternal {

    @Override
    public String getArtifactType() {
        return "whl";
    }
}
