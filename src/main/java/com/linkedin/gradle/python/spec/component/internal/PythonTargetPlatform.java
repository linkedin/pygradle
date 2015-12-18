package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;


public interface PythonTargetPlatform extends PythonToolChain {

  PythonExecutable getSystemPythonExecutable();

  PythonVersion getVersion();

}
