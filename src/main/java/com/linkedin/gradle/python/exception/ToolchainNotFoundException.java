package com.linkedin.gradle.python.exception;

import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import org.gradle.api.GradleException;

public class ToolchainNotFoundException extends GradleException {
    public ToolchainNotFoundException(PythonTargetPlatform targetPlatform) {
        super("Unable to find python for version " + targetPlatform.getVersionAsString());
    }
}
