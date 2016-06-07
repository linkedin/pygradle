package com.linkedin.gradle.python.extension;

import java.io.File;
import org.gradle.api.Project;


public class WheelExtension {

  private File wheelCache;

  public WheelExtension(Project project) {
    wheelCache = new File(project.getBuildDir(), "wheel-cache");
  }

  public File getWheelCache() {
    return wheelCache;
  }

  public void setWheelCache(File wheelCache) {
    this.wheelCache = wheelCache;
  }
}
