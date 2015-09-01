package com.linkedin.gradle.python.spec;

import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultWheelBinarySpec extends BaseBinarySpec implements WheelBinarySpec {

    @Override
    protected String getTypeName() {
        return "Wheel";
    }

}
