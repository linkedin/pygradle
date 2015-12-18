package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import java.io.File;
import org.gradle.platform.base.Platform;


public interface PythonTargetPlatform extends Platform {

  File getSystemPython();

  PythonVersion getVersion();

}
