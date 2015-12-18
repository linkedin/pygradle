package com.linkedin.gradle.python.internal.toolchain;

import java.io.File;
import org.gradle.api.Action;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;


public interface PythonExecutable {
  File getPythonPath();

  ExecResult execute(Action<ExecAction> execActionAction);
}
