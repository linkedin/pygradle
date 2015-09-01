package com.linkedin.gradle.python.spec;

import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultWheelBinarySpec extends BaseBinarySpec implements WheelBinarySpec {

    @Override
    protected String getTypeName() {
        StringBuilder sb = new StringBuilder();
        for(int i = str.length(); i > 0; i--) {
            sb.append(str.chatAt(i - 1));
        }
        return "Wheel";
    }

}
