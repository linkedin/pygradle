package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;

import java.io.File;


public interface PythonEnvironment {

    String getEnvironmentSetupTaskName();

    PythonExecutable getVirtualEnvPythonExecutable();

    PythonExecutable getSystemPythonExecutable();

    File getVenvDir();

    PythonVersion getVersion();

    File getVendorDir();

    File getBuildDir();

    File getPythonBuildDir();

    String getEnvironmentName();
}
