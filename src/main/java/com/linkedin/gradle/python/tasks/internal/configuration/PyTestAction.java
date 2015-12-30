package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import com.linkedin.gradle.python.tasks.internal.utilities.PipOutputStreamProcessor;

import java.io.File;

public class PyTestAction extends BasePythonTaskAction<PythonTestTask> {

    public PyTestAction(File pythonBuildDir, File virtualEnvDir, PythonToolChain toolChain) {
        super(pythonBuildDir, virtualEnvDir, toolChain);
    }

    @Override
    public void configure(PythonTestTask task) {

    }
}