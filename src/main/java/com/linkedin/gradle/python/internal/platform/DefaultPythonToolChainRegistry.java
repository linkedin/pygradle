package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonToolChainRegistry implements PythonToolChainRegistry {

  private final ExecActionFactory execActionFactory;

  public DefaultPythonToolChainRegistry(ExecActionFactory execActionFactory) {

    this.execActionFactory = execActionFactory;
  }

  @Override
  public PythonToolChain getForPlatform(PythonPlatform targetPlatform) {
    return new DefaultPythonToolChain(execActionFactory, targetPlatform.getVersion());
  }
}
