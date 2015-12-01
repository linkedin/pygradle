package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import org.gradle.platform.base.binary.BaseBinarySpec;

import java.io.File;

public class DefaultWheelBinarySpec extends BaseBinarySpec implements WheelBinarySpec {

    private File virtualEnvDir;
    private File pythonBuildDir;
    private PythonPlatform pythonPlatform;
    private PythonToolChain pythonToolchain;
    private WheelComponentSpec wheelComponentSpec;

    @Override
    protected String getTypeName() {
        return "Wheel";
    }

    @Override
    public PythonPlatform getTargetPlatform() {
        return pythonPlatform;
    }

    @Override
    public void setTargetPlatform(PythonPlatform platform) {
        this.pythonPlatform = platform;
    }

    @Override
    public PythonToolChain getToolChain() {
        return pythonToolchain;
    }

    @Override
    public void setComponentSpec(WheelComponentSpec spec) {
        this.wheelComponentSpec = spec;
    }

    @Override
    public WheelComponentSpec getComponentSpec() {
        return wheelComponentSpec;
    }

    @Override
    public void setToolChain(PythonToolChain toolChain) {
        this.pythonToolchain = toolChain;
    }

    @Override
    public void setVirtualEnvDir(File virtualEnvDir) {
        this.virtualEnvDir = virtualEnvDir;
    }

    @Override
    public File getVirtualEnvDir() {
        return virtualEnvDir;
    }

    @Override
    public void setPythonBuildDir(File pythonBuildDir) {
        this.pythonBuildDir = pythonBuildDir;
    }

    @Override
    public File getPythonBuildDir() {
        return pythonBuildDir;
    }
}
