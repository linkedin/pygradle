package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.plugin.internal.PythonBinarySpecAction;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;

public class PythonWheelSpecAction extends PythonBinarySpecAction<WheelBinarySpec> {

    private final PythonPlatform pythonPlatform;

    public PythonWheelSpecAction(PythonPlatform pythonPlatform) {
        this.pythonPlatform = pythonPlatform;
    }

    @Override
    public void execute(WheelBinarySpec spec) {
        spec.setTargetPlatform(pythonPlatform);
    }
}
