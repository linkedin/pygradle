package com.linkedin.gradle.python.internal;

import com.linkedin.gradle.python.internal.platform.DefaultPythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.platform.base.internal.PlatformRequirement;
import org.gradle.platform.base.internal.PlatformResolver;


public class PythonPlatformResolver implements PlatformResolver<PythonPlatform> {


  private final List<PythonPlatform> platforms = new ArrayList<PythonPlatform>();

  public PythonPlatformResolver() {
    for (PythonVersion pythonVersion : PythonVersion.values()) {
      platforms.add(new DefaultPythonPlatform(pythonVersion));
    }
  }

  @Override
  public Class<PythonPlatform> getType() {
    return PythonPlatform.class;
  }

  @Override
  public PythonPlatform resolve(PlatformRequirement platformRequirement) {
    for (PythonPlatform platform : platforms) {
      if (platform.getName().equals(platformRequirement.getPlatformName())) {
        return platform;
      }
    }
    return null;
  }
}
