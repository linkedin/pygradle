package com.linkedin.gradle.python.tasks


import com.linkedin.gradle.python.internal.toolchain.PythonExecutable
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain
import org.gradle.api.Action
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecAction

public class PythonToolchainBuilder {

  PythonExecutable pythonExecutable

  PythonToolchainBuilder() {
  }

  PythonToolchainBuilder withPythonExecutable(ExecAction execAction, int exitCode) {
    pythonExecutable = new PythonExecutable() {
      @Override
      File getPythonPath() {
        return null
      }

      @Override
      ExecResult execute(Action<ExecAction> action) {
        action.execute(execAction)
        return [
            getExitValue: { -> exitCode },
            assertNormalExitValue: { -> if(exitCode != 0) { throw new RuntimeException()} }
        ] as ExecResult
      }
    }
    return this
  }

  public PythonToolChain build() {
    return [
        getPythonExecutable: { -> pythonExecutable },
        getLocalPythonExecutable: { foo -> pythonExecutable}
    ] as PythonToolChain
  }
}
