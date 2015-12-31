package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;

import java.io.File;

public class ResolvedPythonEnvironment {
    private final File buildDir;
    private final File venvDir;
    private final PythonTargetPlatform targetPlatform;

    public ResolvedPythonEnvironment(File buildDir, File venvDir, PythonTargetPlatform targetPlatform) {
        this.buildDir = buildDir;
        this.venvDir = venvDir;
        this.targetPlatform = targetPlatform;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public File getVenvDir() {
        return venvDir;
    }

    public PythonTargetPlatform getTargetPlatform() {
        return targetPlatform;
    }
}
