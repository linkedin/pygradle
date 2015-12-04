package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.internal.platform.DefaultPythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.InstallLocalProject;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.ModelMap;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SharedPythonInfrastructure {


    public static List<PythonPlatform> resolvePlatforms(final PlatformResolvers platformResolver, PythonComponentSpec wheelComponentSpec) {
        List<PlatformRequirement> targetPlatforms = wheelComponentSpec.getTargetPlatforms();
        if (targetPlatforms.isEmpty()) {
            targetPlatforms = Collections.singletonList(
                    DefaultPlatformRequirement.create(DefaultPythonPlatform.current().getName()));
        }
        return CollectionUtils.collect(targetPlatforms, new Transformer<PythonPlatform, PlatformRequirement>() {
            @Override
            public PythonPlatform transform(PlatformRequirement platformRequirement) {
                return platformResolver.resolve(PythonPlatform.class, platformRequirement);
            }
        });
    }
    final PythonVersion version;
    final String componentName;
    final File pythonBuildDir;
    final File virtualEnvDir;
    final PythonPlatform pythonPlatform;

    public SharedPythonInfrastructure(final PythonPlatform pythonPlatform,
                                      final PythonBinarySpec binary,
                                      final BuildDirHolder buildDirHolder) {
        this.pythonPlatform = pythonPlatform;
        this.version = pythonPlatform.getVersion();
        this.componentName = binary.getName();
        this.pythonBuildDir = new File(buildDirHolder.getDir(), String.format("python-%s-%s", componentName, version.getVersionString()));
        this.virtualEnvDir = new File(pythonBuildDir, "venv");
    }

    public String installPythonEnv(ModelMap<Task> tasks,
                                   final PythonPluginConfigurations configurations,
                                   final PythonToolChainRegistry pythonToolChainRegistry) {

        PythonToolChain toolChain = pythonToolChainRegistry.getForPlatform(pythonPlatform);

        final String createVirtualEnv = taskNameGenerator(version, "createVirtualEnv");
        tasks.create(createVirtualEnv, VirtualEnvironmentBuild.class,
                new BasePythonTaskAction<VirtualEnvironmentBuild>(pythonBuildDir, virtualEnvDir, toolChain) {
                    @Override
                    public void configure(VirtualEnvironmentBuild task) {
                        task.setVirtualEnvFiles(configurations.getBootstrap().getConfiguration());
                        task.setActivateScriptName(String.format("activate-%s-%s", componentName, version.getVersionString()));
                    }
                });

        final String installDependencies = taskNameGenerator(version, "installRequiredDependencies");
        tasks.create(installDependencies, InstallDependenciesTask.class,
                new BasePythonTaskAction<InstallDependenciesTask>(pythonBuildDir, virtualEnvDir, toolChain) {
                    @Override
                    public void configure(InstallDependenciesTask task) {
                        task.dependsOn(createVirtualEnv);
                        task.setVirtualEnvFiles(configurations.getVirtualEnv().getConfiguration());
                    }
                });

        final String installRuntimeDependencies = taskNameGenerator(version, "installRuntimeDependencies");
        tasks.create(installRuntimeDependencies, InstallDependenciesTask.class,
                new BasePythonTaskAction<InstallDependenciesTask>(pythonBuildDir, virtualEnvDir, toolChain) {
                    @Override
                    public void configure(InstallDependenciesTask task) {
                        task.dependsOn(installDependencies);
                        task.setVirtualEnvFiles(configurations.getPython().getConfiguration());
                    }
                });

        final String installTestDependencies = taskNameGenerator(version, "installTestDependencies");
        tasks.create(installTestDependencies, InstallDependenciesTask.class,
                new BasePythonTaskAction<InstallDependenciesTask>(pythonBuildDir, virtualEnvDir, toolChain) {
                    @Override
                    public void configure(InstallDependenciesTask task) {
                        task.dependsOn(installRuntimeDependencies);
                        task.setVirtualEnvFiles(configurations.getPyTest().getConfiguration());
                    }
                });

        final String installEditable = taskNameGenerator(version, "installEditable");
        tasks.create(installEditable, InstallLocalProject.class,
                new BasePythonTaskAction<InstallLocalProject>(pythonBuildDir, virtualEnvDir, toolChain) {
                    @Override
                    public void configure(InstallLocalProject task) {
                        task.dependsOn(installTestDependencies);
                    }
                });

        return installEditable;
    }

    public PythonVersion getVersion() {
        return version;
    }

    public String getComponentName() {
        return componentName;
    }

    public File getPythonBuildDir() {
        return pythonBuildDir;
    }

    public File getVirtualEnvDir() {
        return virtualEnvDir;
    }

    public PythonPlatform getPythonPlatform() {
        return pythonPlatform;
    }

    public static String taskNameGenerator(PythonVersion version, String taskName) {
        String camelCaseTaskName = GUtil.toLowerCamelCase(taskName);
        return String.format("%s%s", camelCaseTaskName, version.getVersionString());
    }
}
