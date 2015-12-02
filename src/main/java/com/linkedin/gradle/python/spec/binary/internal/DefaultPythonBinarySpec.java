package com.linkedin.gradle.python.spec.binary.internal;

import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultPythonBinarySpec extends BaseBinarySpec implements PythonBinarySpec {

    @Override
    protected String getTypeName() {
        return "Wheel";
    }
}
