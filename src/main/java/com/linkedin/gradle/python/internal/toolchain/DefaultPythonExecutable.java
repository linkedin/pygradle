package com.linkedin.gradle.python.internal.toolchain;

import java.io.File;
import org.gradle.api.Action;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonExecutable implements PythonExecutable {

  private final ExecActionFactory execActionFactory;
  private final File file;

  public DefaultPythonExecutable(ExecActionFactory execActionFactory, File file) {
    this.execActionFactory = execActionFactory;
    this.file = file;
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public ExecResult execute(Action<ExecAction> execActionAction) {
    ExecAction execAction = execActionFactory.newExecAction();
    execAction.executable(file);
    execActionAction.execute(execAction);
    return execAction.execute();
  }
}
