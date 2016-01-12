package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.*;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.GFileUtils;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;


public class VirtualEnvironmentBuild extends BasePythonTask {

    private Configuration virtualEnvFiles;
    private String activateScriptName;

    @Inject
    protected FileOperations getFileOperations() {
        // Decoration takes care of the implementation
        throw new UnsupportedOperationException();
    }

    @OutputFile
    public File getLocalPythonExecutable() {
        return new File(getVenvDir(), "bin/python");
    }

    @OutputFile
    public File getActivateScript() {
        return new File(getProject().getProjectDir(), activateScriptName);
    }

    @TaskAction
    public void doWork() {
        if (null == virtualEnvFiles) {
            throw new GradleException("Virtual Env must be defined");
        }

        final File vendorDir = getPythonEnvironment().getVendorDir();
        final String virtualEnvDependencyVersion = findVirtualEnvDependencyVersion();

        for (final File file : getVirtualEnvFiles()) {
            getFileOperations().copy(new Action<CopySpec>() {
                @Override
                public void execute(CopySpec copySpec) {
                    if (file.getName().endsWith(".whl")) {
                        copySpec.from(getFileOperations().zipTree(file));
                        copySpec.into(new File(vendorDir, "virtualenv-" + virtualEnvDependencyVersion));
                    } else {
                        copySpec.from(getFileOperations().tarTree(file));
                        copySpec.into(vendorDir);
                    }
                }
            });
        }

        final String path = String.format("%s/virtualenv-%s/virtualenv.py", vendorDir.getAbsolutePath(), virtualEnvDependencyVersion);
        final PythonExecutable pythonExecutable = getPythonEnvironment().getSystemPythonExecutable();

        pythonExecutable.execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.args(path, "--python", pythonExecutable.getPythonPath().getAbsolutePath(), getVenvDir().getAbsolutePath());
            }
        }).assertNormalExitValue();

        File source = new File(getVenvDir(), "bin/activate");
        GFileUtils.copyFile(source, getActivateScript());

        getActivateScript().setExecutable(true);
    }

    private String findVirtualEnvDependencyVersion() {
        ResolvedConfiguration resolvedConfiguration = getVirtualEnvFiles().getResolvedConfiguration();
        Set<ResolvedDependency> virtualEnvDependencies = resolvedConfiguration.getFirstLevelModuleDependencies(new VirtualEvnSpec());
        if (virtualEnvDependencies.isEmpty()) {
            throw new GradleException("Unable to find virtualenv dependency");
        }

        VersionNumber highest = new VersionNumber(0, 0, 0, null);
        for (ResolvedDependency resolvedDependency : virtualEnvDependencies) {
            VersionNumber test = VersionNumber.parse(resolvedDependency.getModuleVersion());
            if (test.compareTo(highest) > 0) {
                highest = test;
            }
        }

        return highest.toString();
    }

    @InputFiles
    Configuration getVirtualEnvFiles() {
        return virtualEnvFiles;
    }

    public void setVirtualEnvFiles(Configuration configuration) {
        this.virtualEnvFiles = configuration;
    }

    public void setActivateScriptName(String activateScriptName) {
        this.activateScriptName = activateScriptName;
    }

    private class VirtualEvnSpec implements Spec<Dependency> {

        @Override
        public boolean isSatisfiedBy(Dependency element) {
            return "virtualenv".equals(element.getName());
        }
    }
}
