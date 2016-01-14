package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecException;


public class PythonEnvironmentTestDouble implements PythonEnvironment {
  private final ExecAction execAction;
  private final int exitCode;
  private final PythonVersion pythonVersion;

  public PythonEnvironmentTestDouble(String pythonVersion, ExecAction execAction, int exitCode) {
    this.pythonVersion = PythonVersion.parse(pythonVersion);
    this.execAction = execAction;
    this.exitCode = exitCode;
  }

  public PythonEnvironmentTestDouble(ExecAction execAction, int exitCode) {
    this("2.7", execAction, exitCode);
  }

  @Override
  public String getEnvironmentSetupTaskName() {
    return "mockedEnvironmentalSetupTask";
  }

  @Override
  public PythonExecutable getVirtualEnvPythonExecutable() {
    return new PythonExecutable() {
      @Override
      public File getPythonPath() {
        return null;
      }

      @Override
      public ExecResult execute(Action<ExecAction> action) {
        action.execute(execAction);
        return new ExecResult() {
          public int getExitValue() {
            return exitCode;
          }

          public ExecResult assertNormalExitValue() {
            if (exitCode != 0) {
              throw new RuntimeException();
            }
            return this;
          }

          @Override
          public ExecResult rethrowFailure()
              throws ExecException {
            throw new RuntimeException();
          }
        };
      }
    };
  }

  @Override
  public PythonExecutable getSystemPythonExecutable() {
    return getVirtualEnvPythonExecutable();
  }

  @Override
  public File getVenvDir() {
    return new File("/foo/build/venv");
  }

  @Override
  public PythonVersion getVersion() {
    return pythonVersion;
  }

  @Override
  public File getVendorDir() {
    return new File("/foo/build/vendor");
  }

  @Override
  public File getBuildDir() {
    return new File("/foo/build");
  }

  @Override
  public File getPythonBuildDir() {
    return new File("/foo/build");
  }

  @Override
  public String getEnvironmentName() {
    return "mockedEnv";
  }
}
