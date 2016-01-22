package com.linkedin.gradle.python.spec.binary.internal;

import org.gradle.api.Task;


public class DefaultSourceDistBinarySpec extends DefaultPythonBinarySpec implements SourceDistBinarySpecInternal {

  private String artifactType = "gztar";

  @Override
  public void setArtifactType(String type) {
    this.artifactType = type;
  }

  @Override
  public String getArtifactType() {
    return artifactType;
  }
}
