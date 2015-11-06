package com.linkedin.gradle.python.internal.platform;

import org.gradle.platform.base.Platform;


public interface PythonPlatform extends Platform {

  PythonVersion getVersion();

}
