package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec, PythonComponentSpecInternal {

  private final Set<String> targetPlatforms = new HashSet<String>();
  private DefaultPythonEnvironmentContainer pythonEnvironmentContainer;

  private File buildDir;
  private ExecActionFactory execActionFactory;

  @Override
  protected String getTypeName() {
    return "Python application";
  }

  @Override
  public void targetPlatform(String targetPlatform) {
    targetPlatforms.add(targetPlatform);
  }

  @Override
  public void setBuildDir(File buildDir) {
    this.buildDir = buildDir;
  }

  @Override
  public File getBuildDir() {
    return buildDir;
  }

  @Override
  public void setExecActionFactory(ExecActionFactory execActionFactory) {
    this.execActionFactory = execActionFactory;
  }

  @Override
  public PythonEnvironmentContainer getPythonEnvironments() {
    if (pythonEnvironmentContainer == null) {
      pythonEnvironmentContainer = new DefaultPythonEnvironmentContainer(buildDir, getName(), execActionFactory);
    }

    pythonEnvironmentContainer.register(targetPlatforms);

    return pythonEnvironmentContainer;
  }
}
