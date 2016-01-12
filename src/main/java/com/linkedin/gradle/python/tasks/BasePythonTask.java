package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;


public class BasePythonTask extends DefaultTask {

  PythonEnvironment pythonEnvironment;

  @Input
  public PythonVersion getPythonVersion() {
    return pythonEnvironment.getVersion();
  }

  public ExecResult execute(Action<ExecAction> action) {
    return pythonEnvironment.getPythonExecutable().execute(action);
  }

  public PythonEnvironment getPythonEnvironment() {
    return pythonEnvironment;
  }

  public void setPythonEnvironment(PythonEnvironment pythonEnvironment) {
    this.pythonEnvironment = pythonEnvironment;
  }

  public File getVenvDir() {
    return pythonEnvironment.getVenvDir();
  }
}
