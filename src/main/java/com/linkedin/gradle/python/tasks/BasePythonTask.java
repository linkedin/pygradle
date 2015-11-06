package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;


public class BasePythonTask extends DefaultTask {

  PythonToolChain pythonToolChain;
  File venvDir;
  File pythonBuilDir;

  public PythonToolChain getPythonToolChain() {
    return pythonToolChain;
  }

  public void setPythonToolChain(PythonToolChain pythonToolChain) {
    this.pythonToolChain = pythonToolChain;
  }

  @Input
  public PythonVersion getPythonVersion() {
    return getPythonToolChain().getVersion();
  }

  @Input
  public File getVenvDir() {
    return venvDir;
  }

  public void setVenvDir(File venvDir) {
    this.venvDir = venvDir;
  }

  public File getPythonBuilDir() {
    return pythonBuilDir;
  }

  public void setPythonBuilDir(File pythonBuilDir) {
    this.pythonBuilDir = pythonBuilDir;
  }
}
