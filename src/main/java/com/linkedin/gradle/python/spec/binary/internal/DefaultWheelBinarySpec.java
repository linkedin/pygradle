package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;


public class DefaultWheelBinarySpec extends DefaultPythonBinarySpec implements WheelBinarySpec {

    private PythonTargetPlatform pythonPlatform;

    public DefaultWheelBinarySpec() {
        super("Wheel");
    }

    @Override
    public File getPythonBuildDir() {
        return new File(getBuildDir(), String.format("python-%s-%s", getName(), getTargetPlatform().getVersion().getVersionString()));
    }

    @Override
    public File getVirtualEnvDir() {
        return new File(getPythonBuildDir(), "venv");
    }

    @Override
    public PythonTargetPlatform getTargetPlatform() {
        return pythonPlatform;
    }

    @Override
    public void setTargetPlatform(PythonTargetPlatform platform) {
        this.pythonPlatform = platform;
    }

    @Override
    public String getProjectSetupTask() {
        return String.format("%sProjectSetup", getName());
    }
}
