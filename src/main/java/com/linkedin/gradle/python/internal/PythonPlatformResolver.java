package com.linkedin.gradle.python.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.util.ArrayList;
import java.util.List;
import org.gradle.platform.base.internal.PlatformRequirement;
import org.gradle.platform.base.internal.PlatformResolver;


public class PythonPlatformResolver implements PlatformResolver<PythonTargetPlatform> {

  private final List<PythonTargetPlatform> platforms = new ArrayList<PythonTargetPlatform>();

  public PythonPlatformResolver() {
    for (PythonVersion pythonVersion : PythonVersion.values()) {
//      platforms.add(new DefaultPythonPlatform(pythonVersion));
    }
  }

  @Override
  public Class<PythonTargetPlatform> getType() {
    return PythonTargetPlatform.class;
  }

  @Override
  public PythonTargetPlatform resolve(PlatformRequirement platformRequirement) {
    for (PythonTargetPlatform platform : platforms) {
      if (platform.getName().equals(platformRequirement.getPlatformName())) {
        return platform;
      }
    }
    return null;
  }
}
