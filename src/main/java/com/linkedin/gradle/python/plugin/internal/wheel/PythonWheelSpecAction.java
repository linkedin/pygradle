package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.plugin.internal.PythonBinarySpecAction;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;


public class PythonWheelSpecAction extends PythonBinarySpecAction<WheelBinarySpec> {

    private final PythonTargetPlatform pythonPlatform;
    private final File buildDir;

    public PythonWheelSpecAction(PythonTargetPlatform pythonPlatform, File buildDir) {
        this.pythonPlatform = pythonPlatform;
        this.buildDir = buildDir;
    }

    @Override
    public void execute(WheelBinarySpec spec) {
        spec.setBuildDir(buildDir);
        spec.setTargetPlatform(pythonPlatform);
    }


}
