package com.linkedin.gradle.python.tasks.internal;

import java.io.File;
import java.util.Arrays;

public class PipLocalInstallAction {

    final File venvDir;
    final PipOutputStreamProcessor pipProcessor = new PipOutputStreamProcessor();

    public PipLocalInstallAction(File venvDir) {
        this.venvDir = venvDir;
    }

    public PipInstallExecAction install(File dependency) {
        return new PipInstallExecAction(venvDir, pipProcessor, Arrays.asList("--editable", dependency.getAbsolutePath()));
    }

    public String getWholeText() {
        return pipProcessor.getWholeText();
    }
}