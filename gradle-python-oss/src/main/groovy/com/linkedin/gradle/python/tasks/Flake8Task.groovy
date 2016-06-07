package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper
import groovy.transform.CompileStatic
import org.gradle.process.ExecResult

@CompileStatic
public class Flake8Task extends AbstractPythonMainSourceDefaultTask {

  public void preExecution() {
    args(VirtualEnvExecutableHelper.getExecutable(component, "bin/flake8").absolutePath,
        "--config", "$component.setupCfg",
        "$component.srcDir",
        "$component.testDir")
  }

  @Override
  void processResults(ExecResult execResult) {
  }
}
