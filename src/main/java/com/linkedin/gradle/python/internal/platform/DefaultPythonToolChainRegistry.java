package com.linkedin.gradle.python.internal.platform;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonToolChainRegistry implements PythonToolChainRegistry {

  private final ExecActionFactory execActionFactory;

  public DefaultPythonToolChainRegistry(ExecActionFactory execActionFactory) {

    this.execActionFactory = execActionFactory;
  }

  @Override
  public PythonToolChain getForPlatform(PythonTargetPlatform targetPlatform) {
    return new DefaultPythonToolChain(execActionFactory, targetPlatform);
  }
}
