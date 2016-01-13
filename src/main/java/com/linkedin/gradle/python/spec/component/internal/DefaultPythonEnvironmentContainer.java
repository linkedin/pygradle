package com.linkedin.gradle.python.spec.component.internal;

import com.google.common.base.Objects;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.component.PythonEnvironmentBuilder;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonEnvironmentContainer implements PythonEnvironmentContainer {

  private static final Logger logger = Logging.getLogger(DefaultPythonEnvironmentContainer.class);

  private final Map<PythonVersion, PythonEnvironment> pythonEnvironmentMap = new LinkedHashMap<PythonVersion, PythonEnvironment>();
  private final Set<String> definitionToPythonEnvMap = new HashSet<String>();
  private final ExecActionFactory execActionFactory;
  private final File buildDir;
  private final String name;

  DefaultPythonEnvironmentContainer(File buildDir, String name, ExecActionFactory execActionFactory) {
    this.buildDir = buildDir;
    this.name = name;
    this.execActionFactory = execActionFactory;
  }

  @Override
  public void register(String targetPlatform) {
    if (!definitionToPythonEnvMap.contains(targetPlatform)) {
      logger.debug("Registering python version {}", targetPlatform);
      PythonEnvironment environment = new PythonEnvironmentBuilder(targetPlatform).withBuildDir(buildDir)
          .withExecActionFactory(execActionFactory)
          .withName(name)
          .build();
      definitionToPythonEnvMap.add(targetPlatform);
      pythonEnvironmentMap.put(environment.getVersion(), environment);
    }
  }

  @Override
  public void register(Collection<String> environments) {
    for (String environment : environments) {
      register(environment);
    }
  }

  public PythonEnvironment getPythonEnvironment(String envName) {
    if ("python".equalsIgnoreCase(envName)) {
      return getDefaultPythonEnvironment();
    }

    PythonVersion parse = PythonVersion.parse(envName);

    if (pythonEnvironmentMap.containsKey(parse)) {
      return pythonEnvironmentMap.get(parse);
    }

    String majorMinorVersion = parse.getMajorMinorVersion();
    String majorVersion = parse.getMajorVersion();
    for (PythonEnvironment environment : pythonEnvironmentMap.values()) {
      if (Objects.equal(environment.getVersion().getMajorMinorVersion(), majorMinorVersion)) {
        return environment;
      } else if (Objects.equal(environment.getVersion().getMajorVersion(), majorVersion)) {
        return environment;
      }
    }

    throw new GradleException("Unable to find python with name " + envName);
  }

  @Override
  public Map<PythonVersion, PythonEnvironment> getPythonEnvironments() {
    return Collections.unmodifiableMap(pythonEnvironmentMap);
  }

  @Override
  public PythonEnvironment getDefaultPythonEnvironment() {
    return pythonEnvironmentMap.values().iterator().next();
  }

  @Override
  public boolean isEmpty() {
    return pythonEnvironmentMap.isEmpty();
  }

  @Override
  public int size() {
    return pythonEnvironmentMap.size();
  }

  @Override
  public String toString() {
    return String.format("DefaultPythonEnvironmentContainer{pythonEnvironmentMap=%s, buildDir=%s, name='%s'}",
        pythonEnvironmentMap, buildDir, name);
  }
}
