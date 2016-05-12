package com.linkedin.gradle.python.tasks


import groovy.transform.CompileStatic
import org.gradle.process.ExecResult

@CompileStatic
public class Flake8Task extends AbstractPythonMainSourceDefaultTask {

  public void preExecution() {
    args("${component.flake8Location}",
        "--config", "$component.setupCfg",
        "$component.srcDir",
        "$component.testDir")
  }

  @Override
  void processResults(ExecResult execResult) {
  }
}
