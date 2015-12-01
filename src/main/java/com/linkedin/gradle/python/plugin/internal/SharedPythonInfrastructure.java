package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.internal.platform.DefaultPythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.InstallLocalProject;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
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

    public static String installPythonEnv(ModelMap<Task> tasks,
                                   final PythonBinarySpec binary,
                                   final PythonVersion version,
                                   final PythonPluginConfigurations configurations,
                                   final String componentName) {

        final String createVirtualEnv = taskNameGenerator(binary, version, "createVirtualEnv");
        tasks.create(createVirtualEnv, VirtualEnvironmentBuild.class, new BasePythonTaskAction<VirtualEnvironmentBuild>(binary) {
            @Override
            public void configure(VirtualEnvironmentBuild task) {
                task.setVirtualEnvFiles(configurations.getBootstrap().getConfiguration());
                task.setActivateScriptName(String.format("activate-%s-%s", componentName, version.getVersionString()));
            }
        });

        final String installDependencies = taskNameGenerator(binary, version, "installRequiredDependencies");
        tasks.create(installDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(createVirtualEnv);
                task.setVirtualEnvFiles(configurations.getVirtualEnv().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "requiredDependencies"));
            }
        });

        final String installRuntimeDependencies = taskNameGenerator(binary, version, "installRuntimeDependencies");
        tasks.create(installRuntimeDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(installDependencies);
                task.setVirtualEnvFiles(configurations.getPython().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "dependencies"));
            }
        });

        final String installTestDependencies = taskNameGenerator(binary, version, "installTestDependencies");
        tasks.create(installTestDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(installRuntimeDependencies);
                task.setVirtualEnvFiles(configurations.getPyTest().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "testDependencies"));
            }
        });

        final String installEditable = taskNameGenerator(binary, version, "installEditable");
        tasks.create(installEditable, InstallLocalProject.class, new BasePythonTaskAction<InstallLocalProject>(binary) {
            @Override
            public void configure(InstallLocalProject task) {
                task.dependsOn(installTestDependencies);
            }
        });

        return installEditable;
    }

    public static String taskNameGenerator(PythonBinarySpec binarySpec, PythonVersion version, String taskName) {
        String binaryName = GUtil.toLowerCamelCase(binarySpec.getName());
        String camelCaseTaskName = GUtil.toCamelCase(taskName);
        return String.format("%s%s%s", binaryName, camelCaseTaskName, version.getVersionString());
    }
}
