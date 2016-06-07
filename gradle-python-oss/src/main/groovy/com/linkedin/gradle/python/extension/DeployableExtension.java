package com.linkedin.gradle.python.extension;

import java.io.File;
import org.gradle.api.Project;


public class DeployableExtension {

  private File deployableBuildDir;
  private File deployableBinDir;
  private File deployableEtcDir;

  public DeployableExtension(Project project) {
    deployableBuildDir = new File(project.getBuildDir(), "deployable");
    deployableBinDir = new File(deployableBuildDir, "bin");
    deployableEtcDir = new File(deployableBuildDir, "etc");
  }

  public File getDeployableBuildDir() {
    return deployableBuildDir;
  }

  public void setDeployableBuildDir(File deployableBuildDir) {
    this.deployableBuildDir = deployableBuildDir;
  }

  public File getDeployableBinDir() {
    return deployableBinDir;
  }

  public void setDeployableBinDir(File deployableBinDir) {
    this.deployableBinDir = deployableBinDir;
  }

  public File getDeployableEtcDir() {
    return deployableEtcDir;
  }

  public void setDeployableEtcDir(File deployableEtcDir) {
    this.deployableEtcDir = deployableEtcDir;
  }
}
