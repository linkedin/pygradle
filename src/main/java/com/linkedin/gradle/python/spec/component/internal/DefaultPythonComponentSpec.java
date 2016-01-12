package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.PythonEnvironmentBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec, PythonComponentSpecInternal {

  private final Set<String> targetPlatforms = new HashSet<String>();
  private final HashMap<String, PythonEnvironment> pythonEnvironmentMap = new HashMap<String, PythonEnvironment>();

  private boolean buildWheels = true;
  private boolean buildSourceDist = true;
  private File buildDir;
  private ExecActionFactory execActionFactory;

  @Override
  protected String getTypeName() {
    return "Python application";
  }

  @Override
  public void buildWheels(boolean wheels) {
    this.buildWheels = wheels;
  }

  @Override
  public boolean getWheels() {
    return buildWheels;
  }

  @Override
  public void buildSourceDist(boolean sourceDist) {
    this.buildSourceDist = sourceDist;
  }

  @Override
  public boolean getSourceDist() {
    return buildSourceDist;
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
  public List<PythonEnvironment> getPythonEnvironments() {
    ArrayList<PythonEnvironment> pythonEnvironments = new ArrayList<PythonEnvironment>(targetPlatforms.size());
    for (String targetPlatform : targetPlatforms) {
      if (!pythonEnvironmentMap.containsKey(targetPlatform)) {
        PythonEnvironment build = new PythonEnvironmentBuilder(targetPlatform).withBuildDir(buildDir)
            .withExecActionFactory(execActionFactory)
            .withName(getName())
            .build();
        pythonEnvironmentMap.put(targetPlatform, build);
      }
      pythonEnvironments.add(pythonEnvironmentMap.get(targetPlatform));
    }

    return pythonEnvironments;
  }
}
