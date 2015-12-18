package com.linkedin.gradle.python.internal.toolchain;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import java.io.File;
import org.gradle.platform.base.ToolChain;


public interface PythonToolChain extends ToolChain {

  PythonVersion getVersion();

  PythonExecutable getSystemPythonExecutable();

  PythonExecutable getLocalPythonExecutable(File pythonBuildDir);
}
