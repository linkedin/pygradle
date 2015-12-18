package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import java.io.File;


public class CreateVirtualEnvConfigureAction extends BasePythonTaskAction<VirtualEnvironmentBuild> {
  private final PythonPluginConfigurations.PythonConfiguration bootstrapConfiguration;
  private final String binarySpecName;
  private final PythonVersion pythonVersion;

  public CreateVirtualEnvConfigureAction(final File pythonBuildDir,
                                         final File virtualEnvDir,
                                         final PythonToolChain toolChain,
                                         final PythonPluginConfigurations.PythonConfiguration bootstrapConfiguration,
                                         final String binarySpecName) {
    super(pythonBuildDir, virtualEnvDir, toolChain);
    this.bootstrapConfiguration = bootstrapConfiguration;
    this.binarySpecName = binarySpecName;
    this.pythonVersion = toolChain.getVersion();
  }

  @Override
  public void configure(VirtualEnvironmentBuild task) {
    task.setVirtualEnvFiles(bootstrapConfiguration.getConfiguration());
    task.setActivateScriptName(String.format("activate-%s-%s", binarySpecName, pythonVersion.getVersionString()));
  }
}
