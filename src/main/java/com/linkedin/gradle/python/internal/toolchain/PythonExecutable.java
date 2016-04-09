package com.linkedin.gradle.python.internal.toolchain;

import org.gradle.api.Action;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;

import java.io.File;


public interface PythonExecutable {
    File getPythonPath();

    ExecResult execute(Action<ExecAction> execActionAction);
}
