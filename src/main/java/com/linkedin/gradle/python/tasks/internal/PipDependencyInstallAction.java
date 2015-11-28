package com.linkedin.gradle.python.tasks.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PipDependencyInstallAction {

    final File venvDir;
    final PipOutputStreamProcessor pipProcessor = new PipOutputStreamProcessor();
    final File outputDir;

    public PipDependencyInstallAction(File venvDir, File outputDir) {
        this.venvDir = venvDir;
        this.outputDir = outputDir;
    }

    public PipInstallExecAction install(File dependency) {
        List<String> dependencies = Arrays.asList("--target", outputDir.getAbsolutePath(),
                "--no-deps", dependency.getAbsolutePath());
        return new PipInstallExecAction(venvDir, pipProcessor, dependencies);
    }

    public String getWholeText() {
        return pipProcessor.getWholeText();
    }

    public Set<String> getPackages() {
        return pipProcessor.getPackages();
    }
}
