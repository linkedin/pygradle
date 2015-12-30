package com.linkedin.gradle.python.tasks.internal.utilities;

import com.linkedin.gradle.python.tasks.internal.PipInstallExecAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PipDependencyInstallAction {

    final File venvDir;
    final PipOutputStreamProcessor pipProcessor = new PipOutputStreamProcessor();

    public PipDependencyInstallAction(File venvDir) {
        this.venvDir = venvDir;
    }

    public PipInstallExecAction install(File dependency) {
        return doInstall(Arrays.asList("--no-deps", dependency.getAbsolutePath()));
    }

    public PipInstallExecAction forceInstall(File dependency) {
        return doInstall(Arrays.asList("--no-deps", "--force-reinstall", dependency.getAbsolutePath()));
    }

    private PipInstallExecAction doInstall(List<String> arguments) {
        return new PipInstallExecAction(venvDir, pipProcessor, arguments);
    }

    public String getWholeText() {
        return pipProcessor.getWholeText();
    }

    public Set<String> getPackages() {
        return pipProcessor.getPackages();
    }
}
