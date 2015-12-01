package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import org.gradle.api.Action;
import org.gradle.language.base.internal.BuildDirHolder;

import java.io.File;

public class PythonBinarySpecAction<T extends PythonBinarySpec, V extends PythonComponentSpec> implements Action<T> {

    final PythonPlatform pythonPlatform;
    final PythonToolChainRegistry pythonToolChainRegistry;
    final BuildDirHolder buildDirHolder;
    final String binaryName;
    final V pythonComponent;

    public PythonBinarySpecAction(PythonPlatform pythonPlatform, PythonToolChainRegistry pythonToolChainRegistry, BuildDirHolder buildDirHolder, String binaryName, V pythonComponent) {
        this.pythonPlatform = pythonPlatform;
        this.pythonToolChainRegistry = pythonToolChainRegistry;
        this.buildDirHolder = buildDirHolder;
        this.binaryName = binaryName;
        this.pythonComponent = pythonComponent;
    }

    @Override
    public void execute(T spec) {
        spec.setTargetPlatform(pythonPlatform);
        spec.setToolChain(pythonToolChainRegistry.getForPlatform(pythonPlatform));

        PythonPlatform platform = spec.getTargetPlatform();
        final File pythonBuildDir = new File(buildDirHolder.getDir(), String.format("python-%s-%s", binaryName, platform.getVersion().getVersionString()));
        final File virtualEnvDir = new File(pythonBuildDir, "venv");

        spec.setVirtualEnvDir(virtualEnvDir);
        spec.setPythonBuildDir(pythonBuildDir);
        spec.setComponentSpec(pythonComponent);
    }
}
