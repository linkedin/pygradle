package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import org.gradle.platform.base.Platform;

import java.io.File;


public interface PythonTargetPlatform extends Platform {

    File getPythonExecutable();

    PythonVersion getVersion();

    String getVersionAsString();

}
